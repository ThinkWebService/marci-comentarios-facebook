package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.repositories.PostRepository;
import com.marcicomentariosfacebook.services.CommentService;
import com.marcicomentariosfacebook.services.PostService;
import com.marcicomentariosfacebook.websocket.CommentWebSocketHandler;
import lombok.AllArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PostServiceImp implements PostService {

    private final PostRepository postRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final CommentService commentService;
    private final CommentWebSocketHandler commentWebSocketHandler;

    @Override
    public Mono<Post> save(Post post) {
        post.setVerb("add"); // marcar como add
        return postRepository.existsById(post.getId())
                .flatMap(exists -> {
                    if (exists) {
                        return postRepository.save(post);
                    } else {
                        return r2dbcEntityTemplate.insert(Post.class).using(post);
                    }
                });
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
    public Mono<Post> editar(Post post) {
        post.setVerb("edited"); // marcar como edited
        return postRepository.findById(post.getId())
                .flatMap(existing -> {
                    existing.setMessage(post.getMessage());
                    existing.setFull_picture(post.getFull_picture());
                    existing.setStatus_type(post.getStatus_type());
                    existing.setStory(post.getStory());
                    existing.setUpdated_time(post.getUpdated_time());
                    existing.setPublished(post.isPublished());
                    existing.setVerb(post.getVerb());
                    return postRepository.save(existing);
                });
    }

    @Override
    public Mono<Post> eliminar(String postId) {
        return postRepository.findById(postId)
                .flatMap(existing -> {
                    // Cambiamos el verb del post a "remove"
                    existing.setVerb("remove");

                    // Guardamos el post primero
                    return postRepository.save(existing)
                            .flatMap(savedPost ->
                                    // Buscamos todos los comentarios asociados a este post
                                    commentService.findByPostId(savedPost.getId())
                                            .flatMap(comment -> {
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
