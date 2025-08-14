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
        return reactionRepository
                .findByUserIdAndPostIdAndCommentId(
                        reaction.getUser_id(),
                        reaction.getPost_id(),
                        reaction.getComment_id()
                )
                .flatMap(existing -> {
                    // Si ya existe, actualiza usando el ID existente
                    reaction.setId(existing.getId());
                    return reactionRepository.save(reaction);
                })
                .switchIfEmpty(
                        // Si no existe, inserta uno nuevo
                        r2dbcEntityTemplate.insert(Reaction.class).using(reaction)
                );
    }

    @Override
    public Mono<Reaction> findById(Long id) {
        return reactionRepository.findById(id);
    }

    @Override
    public Flux<Reaction> findAll() {
        return reactionRepository.findAll();
    }

    @Override
    public Flux<Reaction> findByPostId(String id) {
        return reactionRepository.findReactionsByPostId(id);
    }
}
