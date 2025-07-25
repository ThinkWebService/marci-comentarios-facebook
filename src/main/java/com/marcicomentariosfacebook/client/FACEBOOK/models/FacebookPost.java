package com.marcicomentariosfacebook.client.FACEBOOK.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.CustomDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FacebookPost {
    private String id;
    private String message;
    private String permalink_url;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime created_time;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime updated_time;
    private String story;
    private String status_type;
    private Reactions reactions;
    private CommentsReactionsData comments;
    private String full_picture;
    private AttachmentsWrapper attachments;
}
