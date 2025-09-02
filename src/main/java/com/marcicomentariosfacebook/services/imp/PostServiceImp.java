package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.repositories.PostRepository;
import com.marcicomentariosfacebook.services.CommentService;
import com.marcicomentariosfacebook.services.PostService;
import com.marcicomentariosfacebook.websocket.CommentWebSocketHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public Mono<Post> save(Post post) {
        return postRepository.findById(post.getId())
                .map(existing -> existing.mergeNonNull(post))
                .flatMap(postRepository::save)
                .switchIfEmpty(r2dbcEntityTemplate.insert(Post.class).using(post));
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
}