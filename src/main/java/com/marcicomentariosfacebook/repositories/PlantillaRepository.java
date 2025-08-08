package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Plantilla;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface PlantillaRepository extends ReactiveCrudRepository<Plantilla, String> {

}