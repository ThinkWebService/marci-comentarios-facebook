package com.marcicomentariosfacebook.client.FACEBOOK.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.CustomDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentsReactions {
    private String id;
    private String message;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime created_time;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime updated_time;
    private User from;
    private Reactions reactions;
    private CommentsReactionsData comments;
}