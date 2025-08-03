package com.marcicomentariosfacebook.client.FACEBOOK.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.fechas.CustomDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FacebookPost {
    private String id;
    private String message;
    private String full_picture;
    private String permalink_url;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime created_time;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime updated_time;
    private String story;
    private String status_type;
    @JsonProperty("is_published")
    private boolean published;
    private Reactions reactions;
    private AttachmentsWrapper attachments;
}