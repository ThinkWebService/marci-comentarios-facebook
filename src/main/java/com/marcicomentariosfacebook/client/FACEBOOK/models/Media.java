package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

@Data
public class Media {
    private MediaImage image;
    private String source; // only for videos
}
