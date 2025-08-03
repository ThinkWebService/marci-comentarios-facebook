package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

@Data
public class CommentsReactions {
    private String id;
    private String message;
    private String created_time;
    private String updated_time;
    private User from;
    private Reactions reactions;
    private CommentsReactionsData comments;
}