package com.marcicomentariosfacebook.repositories;

import com.marcicomentariosfacebook.model.Reaction;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactionRepository extends ReactiveCrudRepository<Reaction, Long> {
    @Query("SELECT * FROM reaction WHERE user_id = :userId AND post_id = :postId AND ((:commentId IS NULL AND comment_id IS NULL) OR comment_id = :commentId) LIMIT 1")
    Mono<Reaction> findByUserIdAndPostIdAndCommentId(@Param("userId") String userId, @Param("postId") String postId, @Param("commentId") String commentId);

    @Query("SELECT * FROM reaction WHERE post_id = :postId")
    Flux<Reaction> findReactionsByPostId(@Param("postId") String postId);
}