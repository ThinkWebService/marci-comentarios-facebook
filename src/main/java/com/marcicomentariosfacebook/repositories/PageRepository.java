package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Page;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PageRepository extends ReactiveCrudRepository<Page, String> {

}