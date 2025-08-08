package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.VistaResumen;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VistaResumenService {
    Flux<VistaResumen> findAllResumen();
    Mono<VistaResumen> findById(String postId);
}