package com.marcicomentariosfacebook.dtos;

import lombok.Data;

import java.util.List;

@Data
public class ResponseFacebookDTO {
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
    public static class Value {
        private String item;
        private String verb;
        private String comment_id;
        private Long created_time;
        private String message;
        private String post_id;
        private String parent_id;
        private From from;
        private Post post;
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
        private boolean published;
        private String updated_time;
        private String permalink_url;
        private String promotion_status;
    }
}
