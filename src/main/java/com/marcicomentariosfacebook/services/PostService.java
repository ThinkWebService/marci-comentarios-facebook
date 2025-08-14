package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PostService {
    Mono<Post> save(Post post);
    Mono<Post> findById(String id);

    Flux<Post> findAll();
    Mono<Post> editar(Post post);
    Mono<Post> eliminar(String post_id);

    Mono<Post> setAutoanswered(String post_id, boolean auto_answered);
}