package com.marcicomentariosfacebook.dtos.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    private String id;
    private String message;
    private String full_picture;
    private String permalink_url;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private String story;
    private String status_type;
    private boolean published;
    private List<Reaction> reactions;
    private List<Attachment> attachments;
}