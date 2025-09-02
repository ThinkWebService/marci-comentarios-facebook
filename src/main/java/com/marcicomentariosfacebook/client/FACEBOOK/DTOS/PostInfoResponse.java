package com.marcicomentariosfacebook.client.FACEBOOK.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.fechas.CustomDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostInfoResponse {
    private String id;
    private String message;
    private String permalink_url;
    private String full_picture;
    private String status_type;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime created_time;
    @JsonDeserialize(using = CustomDateDeserializer.class)
    private LocalDateTime updated_time;
    private String story;
    @JsonProperty("is_published")
    private Boolean published;
}