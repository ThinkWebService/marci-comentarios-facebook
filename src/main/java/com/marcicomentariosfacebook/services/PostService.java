package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostService {
    Mono<Post> save(Post post);
    Mono<Post> findById(String id);

    Flux<Post> findAll();
}
