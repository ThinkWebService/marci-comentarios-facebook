package com.marcicomentariosfacebook.dtos;

import lombok.Data;
import java.util.List;

import lombok.Data;
import java.util.List;

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
        private String field; // "feed"
        private Value value;
    }

    @Data
    public static class Value {
        private String item; // "status", "photo", "comment", "reaction"
        private String verb; // "add", "edited", "remove", etc.
        private String post_id;
        private String comment_id;
        private String parent_id;
        private String photo_id;
        private String created_time;
        private String message;
        private String link;
        private boolean published;

        private From from;
        private String reaction_type; // Solo si es reacción
        private List<String> photos;  // Solo si es publicación con fotos
    }

    @Data
    public static class From {
        private String id;
        private String name;
    }
}
