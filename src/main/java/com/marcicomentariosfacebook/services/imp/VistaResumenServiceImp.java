package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.VistaResumen;
import com.marcicomentariosfacebook.repositories.VistaResumenRepository;
import com.marcicomentariosfacebook.services.VistaResumenService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class VistaResumenServiceImp implements VistaResumenService {

    private final VistaResumenRepository vistaResumenRepository;

    @Override
    public Flux<VistaResumen> findAllResumen() {
        return vistaResumenRepository.findAll();
    }

    @Override
    public Mono<VistaResumen> findById(String postId) {
        return vistaResumenRepository.findById(postId);
    }
}
