package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.model.Comment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CommentService {

    Mono<Comment> save(Comment comment);

    Flux<Comment> saveAll(List<Comment>comments);

    Flux<Comment> findAllByParentId(String id);

    Mono<Comment> findById(String id);

    Flux<Comment> findByPostId(String post_id);

    Flux<Comment> findAll();

    Mono<Void> responderComentarioAutomatico(String parent_id, String parent_message);

    Mono<Comment> responderComentarioManual(String parent_id, CommentRequest commentRequest);
    Mono<Comment> editarComentario(String comment_id, CommentRequest commentRequest);
    Mono<Comment> eliminarComentario(String comment_id, String agent_username);
}