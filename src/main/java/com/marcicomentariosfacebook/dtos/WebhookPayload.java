package com.marcicomentariosfacebook.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.fechas.EpochSecondsDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
public class WebhookPayload {
    private String object;
    private List<Entry> entry;

    @Data
    public static class Entry {
        private String id;
        private Long time;
        private List<Change> changes;
    }

    @Data
    public static class Change {
        private String field;
        private Value value;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        private String item;
        private String verb;
        private String post_id;
        private String comment_id;
        private String parent_id;
        private String photo_id;
        private String message;
        private String link;

        @JsonProperty("published")
        private Integer published;

        @JsonProperty("is_published")
        private Boolean isPublished;

        @JsonDeserialize(using = EpochSecondsDateDeserializer.class)
        private LocalDateTime created_time;

        @JsonDeserialize(using = EpochSecondsDateDeserializer.class)
        private LocalDateTime updated_time;

        private From from;
        private String reaction_type;
        private List<String> photos;

        private Post post;

        @JsonProperty("video_id")
        private String videoId;
    }

    @Data
    public static class From {
        private String id;
        private String name;
    }

    @Data
    public static class Post {
        private String id;
        private String status_type;

        @JsonProperty("is_published")
        private Boolean isPublished;

        private String updated_time;
        private String permalink_url;
        private String promotion_status;
    }
}