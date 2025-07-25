package com.marcicomentariosfacebook.client.FACEBOOK.models;

import lombok.Data;

@Data
public class MediaItem {
    private String media_type; // photo, video
    private Media media;
    private String url;
    private String type;
    private Target target;
}
