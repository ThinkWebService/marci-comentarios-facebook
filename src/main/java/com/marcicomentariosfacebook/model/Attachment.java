package com.marcicomentariosfacebook.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("attachment")
public class Attachment {
    @Id
    private String id;
    private String media_type;
    private String media_image;
    private String media_source;
    private String url;
    private LocalDateTime create_time;
    private LocalDateTime update_time;
    private String verb;
    private String message;
    private String post_id;
}