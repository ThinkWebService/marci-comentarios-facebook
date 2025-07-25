package com.marcicomentariosfacebook.client.FACEBOOK.models;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.CustomDateDeserializer;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FacebookApiResponse {
    private PostsWrapper posts;
    private String id;
}

