package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Comment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface CommentRepository extends ReactiveCrudRepository<Comment, String> {
    Flux<Comment> findByParentId(String parentId);
    Flux<Comment> findByPostId(String parentId);
}