package com.marcicomentariosfacebook.client.FACEBOOK.DTOS;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.marcicomentariosfacebook.utils.fechas.CustomDateDeserializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FbCommentsReactionsResp {
    private Comments comments;
    private Reactions reactions;
    private User from;
    private String id;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comments {
        private List<Comment> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String id;
        private String message;
        @JsonDeserialize(using = CustomDateDeserializer.class)
        private LocalDateTime created_time;
        private User from;
        private Parent parent;
        private Reactions reactions;
        private Comments comments;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parent {
        private String created_time;
        private User from;
        private String message;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        private String name;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reactions {
        private List<Reaction> data;
        private Summary summary;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reaction {
        private String id;
        private String name;
        private String type;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        private int total_count;
        private String viewer_reaction;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cursors {
        private String before;
        private String after;
    }
}
