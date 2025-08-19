package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.PlantillaType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface PlantillaTypeService {

    Mono<PlantillaType> save(PlantillaType plantillaType);

    Mono<Boolean> deleteById(Long id);

    Mono<PlantillaType> findById(Long id);

    Flux<PlantillaType> findAll();

    Flux<PlantillaType> saveAll(List<PlantillaType> plantillaTypes);
}