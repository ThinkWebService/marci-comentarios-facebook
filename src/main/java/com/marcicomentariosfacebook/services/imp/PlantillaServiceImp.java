package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Plantilla;
import com.marcicomentariosfacebook.repositories.PlantillaRepository;
import com.marcicomentariosfacebook.services.PlantillaService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class PlantillaServiceImp implements PlantillaService {

    private final PlantillaRepository plantillaRepository;

    @Override
    public Mono<Plantilla> save(Plantilla plantilla) {
        // Si el id es 0, asignamos null para que R2DBC inserte un nuevo registro
        if (plantilla.getId() != null && plantilla.getId() == 0) {
            plantilla.setId(null);
        }
        return plantillaRepository.save(plantilla);
    }

    @Override
    public Mono<Plantilla> findById(Long id) {
        return plantillaRepository.findById(id);
    }

    @Override
    public Flux<Plantilla> findAll() {
        return plantillaRepository.findAll();
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return plantillaRepository.deleteById(id);
    }
}