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
@Table("post")
public class Post {
    @Id
    private String id;
    private String message;
    private String full_picture;
    private String permalink_url;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private String story;
    private String status_type;
    private boolean published;
    private String page_id;
    @Builder.Default
    private String verb = "add";
    private Boolean auto_answered;

    public Post mergeNonNull(Post other) {
        PostBuilder builder = this.toBuilder();

        if (other.getMessage() != null) builder.message(other.getMessage());
        if (other.getFull_picture() != null) builder.full_picture(other.getFull_picture());
        if (other.getPermalink_url() != null) builder.permalink_url(other.getPermalink_url());
        if (other.getCreated_time() != null) builder.created_time(other.getCreated_time());
        if (other.getUpdated_time() != null) builder.updated_time(other.getUpdated_time());
        if (other.getStory() != null) builder.story(other.getStory());
        if (other.getStatus_type() != null) builder.status_type(other.getStatus_type());
        builder.published(other.isPublished());
        if (other.getPage_id() != null) builder.page_id(other.getPage_id());

        // manejo de verb
        if (other.getVerb() == null) {
            // si viene null, setear "add" solo si en BD también estaba vacío
            builder.verb(this.verb != null ? this.verb : "add");
        } else {
            // si viene con valor, sobrescribir siempre
            builder.verb(other.getVerb());
        }

        // manejo de auto_answered
        if (other.getAuto_answered() == null) {
            // si viene null, usar false si en BD estaba null
            builder.auto_answered(this.auto_answered != null ? this.auto_answered : false);
        } else {
            // si viene true o false, sobrescribir siempre
            builder.auto_answered(other.getAuto_answered());
        }

        return builder.build();
    }
}