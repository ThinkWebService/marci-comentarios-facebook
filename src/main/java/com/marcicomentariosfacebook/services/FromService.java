package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.From;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FromService {

    Mono<From> save(From from);

    Mono<From> findById(String id);

    Flux<From> findAll();

    Mono<String> getUserNameByFromId(String fromId);
}