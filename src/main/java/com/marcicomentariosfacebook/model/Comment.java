package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table("comment")
public class Comment {
    @Id
    private String id;
    private String message;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private String verb;
    private Boolean auto_answered;
    private String agent_user;
    private String from_id;
    private String parent_id;
    private String post_id;
    private ResponseType response_type;

    public Comment mergeNonNull(Comment other) {
        CommentBuilder builder = this.toBuilder();

        if (other.getMessage() != null) builder.message(other.getMessage());
        if (other.getCreated_time() != null) builder.created_time(other.getCreated_time());
        if (other.getUpdated_time() != null) builder.updated_time(other.getUpdated_time());
        if (other.getVerb() != null) builder.verb(other.getVerb());
        if (other.getAuto_answered() != null) builder.auto_answered(other.getAuto_answered());
        if (other.getAgent_user() != null) builder.agent_user(other.getAgent_user());
        if (other.getFrom_id() != null) builder.from_id(other.getFrom_id());
        if (other.getParent_id() != null) builder.parent_id(other.getParent_id());
        if (other.getPost_id() != null) builder.post_id(other.getPost_id());
        if (other.getResponse_type() != null) builder.response_type(other.getResponse_type());

        return builder.build();
    }
}
