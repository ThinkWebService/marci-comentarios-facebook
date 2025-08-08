package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.Plantilla;
import com.marcicomentariosfacebook.repositories.PlantillaRepository;
import com.marcicomentariosfacebook.services.PlantillaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PlantillaServiceImp implements PlantillaService {

    private final PlantillaRepository plantillaRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Plantilla> save(Plantilla plantilla) {
        return plantillaRepository.existsById(plantilla.getId())
                .flatMap(exists -> {
                    if (exists) {
                        return plantillaRepository.save(plantilla);
                    } else {
                        return r2dbcEntityTemplate.insert(Plantilla.class).using(plantilla);
                    }
                });
    }

    @Override
    public Flux<Plantilla> saveAll(List<Plantilla> plantillas) {
        return Flux.fromIterable(plantillas)
                .concatMap(this::save);
    }

    @Override
    public Mono<Plantilla> findById(String id) {
        return plantillaRepository.findById(id);
    }

    @Override
    public Flux<Plantilla> findAll() {
        return plantillaRepository.findAll();
    }
}
