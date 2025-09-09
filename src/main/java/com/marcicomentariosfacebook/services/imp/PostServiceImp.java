package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.PostInfoResponse;
import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.repositories.PostRepository;
import com.marcicomentariosfacebook.services.CommentService;
import com.marcicomentariosfacebook.services.PostService;
import com.marcicomentariosfacebook.websocket.CommentWebSocketHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class PostServiceImp implements PostService {

    private final PostRepository postRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final CommentService commentService;
    private final CommentWebSocketHandler commentWebSocketHandler;
    private final APIGraphService apiGraphService;

    @Override
    public Mono<Post> save(Post post) {
        return postRepository.findById(post.getId())
                .flatMap(existing -> {
                    existing.mergeNonNull(post);
                    return postRepository.save(existing);
                })
                .switchIfEmpty(
                        postRepository.findByCreatedTimeAndStatusType(post.getCreated_time(), post.getStatus_type())
                                .flatMap(existing -> Mono.just(existing)) // ya existe, no insertar
                                .switchIfEmpty(r2dbcEntityTemplate.insert(Post.class).using(post)) // no existe, insertar
                );
    }

    @Override
    public Mono<Post> findById(String id) {
        return postRepository.findById(id);
    }

    @Override
    public Flux<Post> findAll() {
        return postRepository.findAll();
    }

    @Override
    public Mono<Post> eliminar(String postId) {
        return postRepository.findById(postId)
                .flatMap(existing -> {
                    log.info("⚠️ Marcando post como remove: {}", existing.getId());

                    // Cambiamos el verb del post a "remove"
                    existing.setVerb("remove");

                    // Guardamos el post primero
                    return postRepository.save(existing)
                            .flatMap(savedPost ->
                                    // Buscamos todos los comentarios asociados a este post
                                    commentService.findByPostId(savedPost.getId())
                                            .flatMap(comment -> {
                                                log.info("⚠️ Marcando comentario como remove: {} (post {})", comment.getId(), savedPost.getId());

                                                // Actualizamos el verb de cada comentario
                                                comment.setVerb("remove");
                                                return commentService.save(comment)
                                                        // Publicamos la actualización en WebSocket
                                                        .flatMap(commentWebSocketHandler::publishComment);
                                            })
                                            .then(Mono.just(savedPost)) // después devolvemos el post guardado
                            );
                });
    }

    @Override
    public Mono<Post> setAutoanswered(String postId, boolean auto_answered) {
        return postRepository.findById(postId)
                .flatMap(existing -> {
                    existing.setAuto_answered(auto_answered);
                    return postRepository.save(existing);
                });
    }

    @Override
    public Flux<Post> setAutoansweredAll(boolean auto_answered) {
        return postRepository.updateAutoAnsweredAll(auto_answered)
                .thenMany(postRepository.findAll());
    }

    @Override
    public Mono<Post> savePostIfNotExist(String post_id, String page_id) {
        return apiGraphService.getPostInfoFromMeta(post_id)
                .flatMap(postInfo -> {
                    // Si Facebook devolvió info, actualizar/guardar normalmente
                    Post post = new Post();
                    post.setId(postInfo.getId());
                    post.setMessage(postInfo.getMessage());
                    post.setFull_picture(postInfo.getFull_picture());
                    post.setPermalink_url(postInfo.getPermalink_url());
                    post.setCreated_time(postInfo.getCreated_time());
                    post.setUpdated_time(postInfo.getUpdated_time());
                    post.setStory(postInfo.getStory());
                    post.setStatus_type(postInfo.getStatus_type());
                    post.setPublished(postInfo.getPublished());
                    post.setPage_id(page_id);
                    post.setVerb("add");
                    return save(post);
                })
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo info o guardando post {}",post_id, e);
                    return Mono.empty();
                });

    }



}