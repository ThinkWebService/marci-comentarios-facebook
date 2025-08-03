package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Reaction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactionService {
    Mono<Reaction> save(Reaction reaction);

    Mono<Reaction> findById(Long id);

    Flux<Reaction> findAll();
}