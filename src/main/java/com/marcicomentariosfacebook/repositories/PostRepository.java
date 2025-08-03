package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Post;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PostRepository extends ReactiveCrudRepository<Post, String> {

}