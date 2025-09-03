package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Post;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PostRepository extends ReactiveCrudRepository<Post, String> {
    @Modifying
    @Query("UPDATE post SET auto_answered = :autoAnswered WHERE auto_answered != :autoAnswered")
    Mono<Integer> updateAutoAnsweredAll(boolean autoAnswered);

}