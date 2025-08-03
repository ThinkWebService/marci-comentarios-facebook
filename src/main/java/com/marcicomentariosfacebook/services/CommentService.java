package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.ResponseType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CommentService {

    Mono<Comment> save(Comment comment);

    Mono<Void> saveAll(List<Comment>comments);



    Mono<Comment> findById(String id);

    Flux<Comment> findAll();

    Mono<Void> responderComentario(String parent_id, String parent_message, boolean auto_answered, ResponseType responseType, String agent_user);

}