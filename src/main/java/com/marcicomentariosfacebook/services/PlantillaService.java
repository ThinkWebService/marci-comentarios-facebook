package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Plantilla;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlantillaService {

    Mono<Plantilla> save(Plantilla plantilla);

    Mono<Void> deleteById(Long id);

    Mono<Plantilla> findById(Long id);

    Flux<Plantilla> findAll();

}