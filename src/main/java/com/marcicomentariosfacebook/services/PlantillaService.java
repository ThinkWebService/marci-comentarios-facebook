package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Plantilla;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlantillaService {

    Mono<Plantilla> save(Plantilla plantilla);

    Mono<Plantilla> findById(String id);

    Flux<Plantilla> findAll();

    Flux<Plantilla> saveAll(List<Plantilla> plantillas);
}