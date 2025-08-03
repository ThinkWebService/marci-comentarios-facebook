package com.marcicomentariosfacebook.utils.maper.responses;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbCommentsReactionsResp;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Reaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperCommentsReactions {

    public Flux<Comment> mapComments(FbCommentsReactionsResp response) {
        if (response.getComments() == null || response.getComments().getData() == null) {
            return Flux.empty();
        }

        List<Comment> orderedComments = new ArrayList<>();

        for (FbCommentsReactionsResp.Comment fbComment : response.getComments().getData()) {
            flattenCommentsRecursively(fbComment, response.getId(), null, orderedComments);
        }

        return Flux.fromIterable(orderedComments);
    }

    private void flattenCommentsRecursively(FbCommentsReactionsResp.Comment fbComment, String postId, String parentId, List<Comment> collector) {
        // Validación de seguridad para evitar NPE
        if (fbComment.getFrom() == null || fbComment.getFrom().getId() == null) {
            log.warn("Comentario sin autor (from) o sin ID. Comentario ignorado: {}", fbComment.getId());
            return; // Salta este comentario
        }

        // Mapea el comentario actual
        Comment mappedComment = Comment.builder()
                .id(fbComment.getId())
                .message(fbComment.getMessage())
                .created_time(fbComment.getCreated_time())
                .from_id(fbComment.getFrom().getId())
                .parent_id(parentId) // se pasa como parámetro en la recursión
                .post_id(postId)
                .build();

        // Agrega al resultado
        collector.add(mappedComment);

        // Recorre sus respuestas (hijos)
        if (fbComment.getComments() != null && fbComment.getComments().getData() != null) {
            for (FbCommentsReactionsResp.Comment child : fbComment.getComments().getData()) {
                flattenCommentsRecursively(child, postId, fbComment.getId(), collector);
            }
        }
    }


    public Flux<Reaction> mapReactions(FbCommentsReactionsResp response) {
        // 1. Reacciones al POST principal
        Flux<Reaction> postReactions = Flux.empty();
        if (response.getReactions() != null) {
            // Mapear reacciones del array 'data'
            postReactions = response.getReactions().getData() != null
                    ? Flux.fromIterable(response.getReactions().getData())
                    .map(reaction -> Reaction.builder()
                            .id(null)
                            .user_id(reaction.getId())
                            .user_name(reaction.getName())
                            .type(reaction.getType())
                            .comment_id(null)
                            .post_id(response.getId())
                            .build())
                    : Flux.empty();

            // Añadir reacción del AUTOR (from) si existe en el summary
            if (response.getReactions().getSummary() != null
                    && !"NONE".equals(response.getReactions().getSummary().getViewer_reaction())
                    && response.getFrom() != null) {

                Reaction authorReaction = Reaction.builder()
                        .id(null)
                        .user_id(response.getFrom().getId()) // ID del autor (from)
                        .user_name(response.getFrom().getName())
                        .type(response.getReactions().getSummary().getViewer_reaction()) // Ej: "LIKE"
                        .comment_id(null)
                        .post_id(response.getId())
                        .build();

                postReactions = Flux.concat(postReactions, Flux.just(authorReaction));
            }
        }

        // 2. Reacciones a COMENTARIOS (misma lógica para el autor del comentario)
        Flux<Reaction> commentReactions = Flux.empty();
        if (response.getComments() != null && response.getComments().getData() != null) {
            commentReactions = Flux.fromIterable(response.getComments().getData())
                    .flatMap(comment -> {
                        if (comment.getReactions() == null) return Flux.empty();

                        Flux<Reaction> reactions = comment.getReactions().getData() != null
                                ? Flux.fromIterable(comment.getReactions().getData())
                                .map(reaction -> Reaction.builder()
                                        .id(null)
                                        .user_id(reaction.getId())
                                        .user_name(reaction.getName())
                                        .type(reaction.getType())
                                        .comment_id(comment.getId())
                                        .post_id(response.getId())
                                        .build())
                                : Flux.empty();

                        // Añadir reacción del AUTOR del comentario (comment.getFrom())
                        if (comment.getReactions().getSummary() != null
                                && !"NONE".equals(comment.getReactions().getSummary().getViewer_reaction())
                                && comment.getFrom() != null) {

                            Reaction authorReaction = Reaction.builder()
                                    .id(null)
                                    .user_id(comment.getFrom().getId()) // ID del autor del comentario
                                    .user_name(comment.getFrom().getName())
                                    .type(comment.getReactions().getSummary().getViewer_reaction())
                                    .comment_id(comment.getId())
                                    .post_id(response.getId())
                                    .build();

                            reactions = Flux.concat(reactions, Flux.just(authorReaction));
                        }

                        return reactions;
                    });
        }

        return Flux.merge(postReactions, commentReactions);
    }
    public Flux<From> mapFroms(FbCommentsReactionsResp response) {
        if (response == null) {
            return Flux.empty();
        }

        // Autor del post
        Flux<From> postAuthor = response.getFrom() != null
                ? Flux.just(From.builder()
                .id(response.getFrom().getId())
                .name(response.getFrom().getName())
                .build())
                : Flux.empty();

        // Usuarios de comentarios y respuestas
        Flux<From> commentUsers = Flux.empty();
        if (response.getComments() != null && response.getComments().getData() != null) {
            commentUsers = Flux.fromIterable(response.getComments().getData())
                    .flatMap(comment -> {
                        Flux<From> users = comment.getFrom() != null
                                ? Flux.just(From.builder()
                                .id(comment.getFrom().getId())
                                .name(comment.getFrom().getName())
                                .build())
                                : Flux.empty();

                        if (comment.getComments() != null && comment.getComments().getData() != null) {
                            Flux<From> replyUsers = Flux.fromIterable(comment.getComments().getData())
                                    .filter(reply -> reply.getFrom() != null)
                                    .map(reply -> From.builder()
                                            .id(reply.getFrom().getId())
                                            .name(reply.getFrom().getName())
                                            .build());
                            users = Flux.concat(users, replyUsers);
                        }

                        // Usuarios de reacciones a comentarios
                        if (comment.getReactions() != null && comment.getReactions().getData() != null) {
                            Flux<From> reactionUsers = Flux.fromIterable(comment.getReactions().getData())
                                    .map(reaction -> From.builder()
                                            .id(reaction.getId())
                                            .name(reaction.getName())
                                            .build());
                            users = Flux.concat(users, reactionUsers);
                        }

                        return users;
                    });
        }

        // Usuarios de reacciones al post
        Flux<From> postReactionUsers = Flux.empty();
        if (response.getReactions() != null && response.getReactions().getData() != null) {
            postReactionUsers = Flux.fromIterable(response.getReactions().getData())
                    .map(reaction -> From.builder()
                            .id(reaction.getId())
                            .name(reaction.getName())
                            .build());
        }

        return Flux.merge(postAuthor, commentUsers, postReactionUsers)
                .distinct(From::getId);
    }
}