package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.repositories.FromRepository;
import com.marcicomentariosfacebook.services.FromService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class FromServiceImp implements FromService {

    private final FromRepository fromRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<From> save(From from) {
        return fromRepository.existsById(from.getId())
                .flatMap(exists -> {
                    if (exists) {
                        return fromRepository.save(from);
                    } else {
                        return r2dbcEntityTemplate.insert(From.class).using(from);
                    }
                });
    }

    @Override
    public Mono<From> findById(String id) {
        return fromRepository.findById(id);
    }

    @Override
    public Flux<From> findAll() {
        return fromRepository.findAll();
    }

    @Override
    public Mono<String> getUserNameByFromId(String id) {
        return Mono.justOrEmpty(id)
                .flatMap(fromRepository::findById)
                .map(From::getName);
    }
}