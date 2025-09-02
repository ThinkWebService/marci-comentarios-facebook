package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.exception.ConflictException;
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
        // Si el id es 0, asignamos null para que R2DBC inserte un nuevo registro
        if (plantillaType.getId() != null && plantillaType.getId() == 0) {
            plantillaType.setId(null);
        }
        return plantillaTypeRepository.save(plantillaType);
    }

    @Override
    public Mono<Boolean> deleteById(Long id) {
        return plantillaRepository.existsByTypeId(id)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("No se puede eliminar: hay plantillas asociadas"));
                    }
                    return plantillaTypeRepository.deleteById(id).thenReturn(true);
                });
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