package com.marcicomentariosfacebook.client.FACEBOOK.DTOS;

import com.marcicomentariosfacebook.client.FACEBOOK.models.PostsWrapper;
import lombok.Data;

@Data
public class FbPostsResp {
    private PostsWrapper posts;
    private String id;
}

