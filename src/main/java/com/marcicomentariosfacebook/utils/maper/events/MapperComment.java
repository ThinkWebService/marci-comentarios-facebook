package com.marcicomentariosfacebook.utils.maper.events;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class MapperComment {
    public Comment mapValueToComment(WebhookPayload.Value value) {
        if (value == null) {
            return null;
        }

        return Comment.builder()
                .id(value.getComment_id() != null ? value.getComment_id() : value.getPost_id())
                .message(value.getMessage())
                .created_time(value.getCreated_time())
                .updated_time(value.getUpdated_time())
                .verb(value.getVerb())
                .from_id(value.getFrom() != null ? value.getFrom().getId() : null)
                .parent_id(value.getParent_id())
                .post_id(value.getPost_id())
                .build();
    }

}
