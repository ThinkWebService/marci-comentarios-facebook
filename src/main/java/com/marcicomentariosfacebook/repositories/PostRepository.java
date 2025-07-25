package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.models.Post;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface PostRepository extends ReactiveMongoRepository<Post, String> {

}