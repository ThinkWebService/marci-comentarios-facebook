package com.marcicomentariosfacebook.client.FACEBOOK.services;

import com.marcicomentariosfacebook.client.FACEBOOK.models.FacebookApiResponse;
import com.marcicomentariosfacebook.models.Attachment;
import com.marcicomentariosfacebook.models.Comment;
import com.marcicomentariosfacebook.models.Post;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface APIGraphService {
    // Listar posts por page id
    Flux<Post> getPosts();

    // Obtener comentarios por attachment
    Mono<Attachment> addCommentsAndReactionsToAttachment(Attachment attachment);

    // Enviar un comentario a un post
    Mono<String> sendComment(String postId, String message);

    // Responder a un comentario
    Mono<String> replyComment(String commentId, String message);
}

