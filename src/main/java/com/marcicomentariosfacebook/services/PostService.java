package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.PostInfoResponse;
import com.marcicomentariosfacebook.model.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostService {
    Mono<Post> save(Post post);
    Mono<Post> findById(String id);

    Flux<Post> findAll();
    Mono<Post> eliminar(String post_id);

    Mono<Post> setAutoanswered(String post_id, boolean auto_answered);
    Flux<Post> setAutoansweredAll(boolean auto_answered);
    Mono<Post> savePostIfNotExist(String post_id, String page_id);
}