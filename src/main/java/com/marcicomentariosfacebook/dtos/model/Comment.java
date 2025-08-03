package com.marcicomentariosfacebook.dtos.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    private String id;
    private String message;
    private String created_time;
    private String updated_time;
    private Author from;

    private List<Reaction> reactions;  // Reacciones del comentario
    private List<Comment> replies;     // Respuestas al comentario

}