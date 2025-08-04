package com.marcicomentariosfacebook.utils.maper.events;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.model.Comment;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
@Component
public class MapperComment {

    public Mono<Comment> mapValueToComment(WebhookPayload.Value value) {
        if (value == null) {
            return Mono.empty();
        }

        String parentId = value.getParent_id();
        String postId = value.getPost_id();
        String commentId = value.getComment_id();

        // Extraemos el prefijo del comment_id antes del guion bajo
        String commentIdPrefix = null;
        if (commentId != null && commentId.contains("_")) {
            commentIdPrefix = commentId.split("_")[0];
        }

        // Si parentId no empieza con el mismo prefijo, lo tratamos como raíz
        if (parentId != null && commentIdPrefix != null && !parentId.startsWith(commentIdPrefix)) {
            parentId = null;
        }

        Comment comment = Comment.builder()
                .id(commentId != null ? commentId : postId)
                .message(value.getMessage())
                .created_time(value.getCreated_time())
                .updated_time(value.getUpdated_time())
                .verb(value.getVerb())
                .from_id(value.getFrom() != null ? value.getFrom().getId() : null)
                .parent_id(parentId)
                .post_id(postId)
                .build();

        return Mono.just(comment);
    }
}
