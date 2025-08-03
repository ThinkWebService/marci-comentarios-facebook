package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("comment")
public class Comment {
    @Id
    private String id;
    private String message;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private String verb; //add, edited, remove, hide
    private boolean auto_answered;
    private String agent_user;

    private String from_id;
    private String parent_id;
    private String post_id;
    private ResponseType response_type;
}