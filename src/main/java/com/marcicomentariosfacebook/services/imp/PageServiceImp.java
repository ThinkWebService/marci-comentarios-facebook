package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Page;
import com.marcicomentariosfacebook.repositories.PageRepository;
import com.marcicomentariosfacebook.services.PageService;
import lombok.AllArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PageServiceImp implements PageService {

    private final PageRepository pageRepository;

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    public Mono<Page> save(Page page) {
        return pageRepository.existsById(page.getId())
            .flatMap(exists -> {
                if (exists) {
                    return pageRepository.save(page);
                } else {
                    return r2dbcEntityTemplate.insert(Page.class).using(page);
                }
            });
    }
}