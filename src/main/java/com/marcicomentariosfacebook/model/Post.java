package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("post")
public class Post {
    @Id
    private String id;
    private String message;
    private String full_picture;
    private String permalink_url;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private String story;
    private String status_type;
    private boolean published;
    private String page_id;
    private String verb;
    private boolean auto_answered;
}