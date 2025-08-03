package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Reaction;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ReactionRepository extends ReactiveCrudRepository<Reaction, Long> {

}