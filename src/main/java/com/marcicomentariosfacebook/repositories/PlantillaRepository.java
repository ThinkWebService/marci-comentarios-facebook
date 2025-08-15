package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Plantilla;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlantillaRepository extends ReactiveCrudRepository<Plantilla, Long> {
    // Devuelve true si hay alguna plantilla con el typeId dado
    Mono<Boolean> existsByTypeId(Long typeId);
}