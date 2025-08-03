package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Page;
import reactor.core.publisher.Mono;

public interface PageService {
    Mono<Page> save(Page page);
}