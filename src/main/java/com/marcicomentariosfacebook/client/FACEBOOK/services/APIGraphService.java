package com.marcicomentariosfacebook.client.FACEBOOK.services;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbCommentsReactionsResp;
import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbPageResp;
import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbPostsResp;
import com.marcicomentariosfacebook.dtos.model.Attachment;
import reactor.core.publisher.Mono;

public interface APIGraphService {
    // Obtener comentarios por post
    Mono<FbCommentsReactionsResp> getCommentsReactionsByPostId(String postId);
    // Listar posts por page id
    Mono<FbPostsResp> getPosts();

    Mono<FbPageResp> getPageInfo();

    // Obtener comentarios por attachment
    Mono<Attachment> addCommentsAndReactionsToAttachment(Attachment attachment);

    // Enviar un comentario a un post
    Mono<String> sendComment(String postId, String message);

    // Responder a un comentario
    Mono<String> replyComment(String commentId, String message);

    Mono<Boolean> deleteComment(String commentId);


}