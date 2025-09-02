package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Plantilla;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PlantillaService {

    Mono<Plantilla> save(Plantilla plantilla);

    Mono<Void> deleteById(Long id);

    Mono<Plantilla> findById(Long id);

    Flux<Plantilla> findAll();

}