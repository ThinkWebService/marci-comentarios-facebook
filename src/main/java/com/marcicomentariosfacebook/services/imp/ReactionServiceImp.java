package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Reaction;
import com.marcicomentariosfacebook.repositories.ReactionRepository;
import com.marcicomentariosfacebook.services.ReactionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class ReactionServiceImp implements ReactionService {

    private final ReactionRepository reactionRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Reaction> save(Reaction reaction) {
        if (reaction.getId() == null) {
            // Si no hay ID, guardamos directamente sin verificar existencia
            return r2dbcEntityTemplate.insert(Reaction.class).using(reaction);
        } else {
            // Si hay ID, verificamos existencia antes de guardar
            return reactionRepository.existsById(reaction.getId())
                    .flatMap(exists -> {
                        if (exists) {
                            return reactionRepository.save(reaction); // Actualiza
                        } else {
                            return r2dbcEntityTemplate.insert(Reaction.class).using(reaction); // Inserta nuevo
                        }
                    });
        }
    }

    @Override
    public Mono<Reaction> findById(Long id) {
        return reactionRepository.findById(id);
    }

    @Override
    public Flux<Reaction> findAll() {
        return reactionRepository.findAll();
    }
}
