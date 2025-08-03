package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.From;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FromRepository extends ReactiveCrudRepository<From, String> {

}