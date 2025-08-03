package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Comment;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CommentRepository extends ReactiveCrudRepository<Comment, String> {

}