package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.PlantillaType;
import com.marcicomentariosfacebook.repositories.PlantillaRepository;
import com.marcicomentariosfacebook.repositories.PlantillaTypeRepository;
import com.marcicomentariosfacebook.services.PlantillaTypeService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class PlantillaTypeServiceImp implements PlantillaTypeService {

    private final PlantillaTypeRepository plantillaTypeRepository;
    private final PlantillaRepository plantillaRepository;

    @Override
    public Mono<PlantillaType> save(PlantillaType plantillaType) {
        return plantillaTypeRepository.save(plantillaType);
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        return plantillaTypeRepository.existsById(id)
                .flatMap(exists -> !exists ? Mono.just(false) :
                        plantillaRepository.existsByTypeId(id)
                                .flatMap(hasPlantillas -> hasPlantillas ? Mono.just(false) :
                                        plantillaTypeRepository.deleteById(id).thenReturn(true)
                                                .onErrorReturn(false)
                                )
                );
    }

    @Override
    public Mono<PlantillaType> findById(Long id) {
        return plantillaTypeRepository.findById(id);
    }

    @Override
    public Flux<PlantillaType> findAll() {
        return plantillaTypeRepository.findAll();
    }

    @Override
    public Flux<PlantillaType> saveAll(List<PlantillaType> plantillaTypes) {
        return plantillaTypeRepository.saveAll(plantillaTypes);
    }
}