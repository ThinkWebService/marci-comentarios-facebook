package com.marcicomentariosfacebook.maper;

import com.marcicomentariosfacebook.client.FACEBOOK.models.CommentsReactions;
import com.marcicomentariosfacebook.client.FACEBOOK.models.CommentsReactionsData;
import com.marcicomentariosfacebook.client.FACEBOOK.models.User;
import com.marcicomentariosfacebook.models.Attachment;
import com.marcicomentariosfacebook.models.Author;
import com.marcicomentariosfacebook.models.Comment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FacebookMapperAttachments {

    public Attachment updateAttachment(Attachment attachment, CommentsReactionsData commentsReactionsData) {

        if (attachment == null || commentsReactionsData == null || commentsReactionsData.getData() == null) {
            return attachment;
        }

        List<CommentsReactions> dataList = commentsReactionsData.getData();

        // Procesar comentarios (pueden tener subcomentarios)
        List<Comment> comments = dataList.stream()
                .map(this::mapToComment)
                .collect(Collectors.toList());

        // Procesar reacciones (puedes tomar todas las reacciones de todos los comentarios, o solo del primero, según lógica)
        List<com.marcicomentariosfacebook.models.Reaction> reactions = dataList.stream()
                .flatMap(cr -> cr.getReactions() != null && cr.getReactions().getData() != null
                        ? cr.getReactions().getData().stream().map(this::mapFacebookReactionToDomain)
                        : Stream.empty())
                .collect(Collectors.toList());

        attachment.setComments(comments);
        attachment.setReactions(reactions);

        return attachment;
    }

    private Comment mapToComment(CommentsReactions cr) {
        return Comment.builder()
                .id(cr.getId())
                .message(cr.getMessage())
                .created_time(cr.getCreated_time())
                .updated_time(cr.getUpdated_time())
                .from(mapToAuthor(cr.getFrom()))
                .reactions(cr.getReactions() != null
                        ? cr.getReactions().getData().stream()
                        .map(this::mapFacebookReactionToDomain)
                        .collect(Collectors.toList())
                        : Collections.emptyList())
                .replies(cr.getComments() != null
                        ? cr.getComments().getData().stream()
                        .map(this::mapToComment) // Llamada recursiva
                        .collect(Collectors.toList())
                        : Collections.emptyList())
                .build();
    }

    private com.marcicomentariosfacebook.models.Reaction mapFacebookReactionToDomain(
            com.marcicomentariosfacebook.client.FACEBOOK.models.Reaction fbReaction) {

        return com.marcicomentariosfacebook.models.Reaction.builder()
                .id(fbReaction.getId())
                .name(fbReaction.getName())
                .type(fbReaction.getType())
                .build();
    }

    private Author mapToAuthor(User fbUser) {
        if (fbUser == null) return null;
        return new Author(fbUser.getId(), fbUser.getName());
    }
}
