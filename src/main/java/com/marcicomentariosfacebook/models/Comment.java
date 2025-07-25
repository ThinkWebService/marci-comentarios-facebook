package com.marcicomentariosfacebook.models;

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
public class Comment {

    private String id;
    private String message;
    private LocalDateTime created_time;
    private LocalDateTime updated_time;
    private Author from;

    private List<Reaction> reactions;  // Reacciones del comentario
    private List<Comment> replies;     // Respuestas al comentario

}