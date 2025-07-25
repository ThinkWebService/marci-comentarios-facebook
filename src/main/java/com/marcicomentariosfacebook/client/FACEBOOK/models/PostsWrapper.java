package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

import java.util.List;

@Data
public class PostsWrapper {
    private List<FacebookPost> data;
}
