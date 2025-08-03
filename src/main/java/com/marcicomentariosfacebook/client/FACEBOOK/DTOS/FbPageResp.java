package com.marcicomentariosfacebook.client.FACEBOOK.DTOS;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FbPageResp {
    private String id;
    private String name;
    private String username;
    private String category;
    private String about;
    private String link;
    private int fan_count;
    private int followers_count;
    private Cover cover;
    private Picture picture;
    @JsonProperty("is_verified")
    private boolean verified;

    @Data
    public static class Cover {
        private String cover_id;
        private int offset_x;
        private int offset_y;
        private String source;
        private String id;
    }

    @Data
    public static class Picture {
        private PictureData data;

        @Data
        public static class PictureData {
            private String url;
        }
    }
}