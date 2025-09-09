package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Post;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface PostRepository extends ReactiveCrudRepository<Post, String> {
    @Modifying
    @Query("UPDATE post SET auto_answered = :autoAnswered WHERE auto_answered != :autoAnswered")
    Mono<Integer> updateAutoAnsweredAll(boolean autoAnswered);

    // Método para buscar por created_time + status_type (Caso especial cuando hay eventos del mismo post con diferente post_id)
    @Query("SELECT * FROM post WHERE created_time = :createdTime AND status_type = :statusType AND page_id = :pageId")
    Mono<Post> findByCreatedTimeAndStatusType(LocalDateTime createdTime, String statusType);

}