package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
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
    @Column("parent_id")
    private String parentId;
    private String postId;
    private ResponseType response_type;
    @Column("previous_version_id")
    private String previousVersionId;     // versionamiento, apunta a la versión anterior

    // 🔹 Campo adicional solo para uso en la vista/DTO, no se guarda en DB
    @Transient
    private String from_name;

    public Comment mergeNonNull(Comment other) {
        CommentBuilder builder = this.toBuilder();

        if (other.getMessage() != null) builder.message(other.getMessage());
        if (other.getCreated_time() != null) builder.created_time(other.getCreated_time());
        if (other.getUpdated_time() != null) builder.updated_time(other.getUpdated_time());
        if (other.getVerb() != null) builder.verb(other.getVerb());
        if (other.getAuto_answered() != null) builder.auto_answered(other.getAuto_answered());
        if (other.getAgent_user() != null) builder.agent_user(other.getAgent_user());
        if (other.getFrom_id() != null) builder.from_id(other.getFrom_id());
        if (other.getParentId() != null) builder.parentId(other.getParentId());
        if (other.getPostId() != null) builder.postId(other.getPostId());
        if (other.getResponse_type() != null) builder.response_type(other.getResponse_type());
        if (other.getPreviousVersionId() != null) builder.previousVersionId(other.getPreviousVersionId());

        return builder.build();
    }
}