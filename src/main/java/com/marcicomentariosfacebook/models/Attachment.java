package com.marcicomentariosfacebook.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    private String id;         // es el id del target
    private String type;       // Ej: "photo", "video", "share"
    private String url;        // URL visible
    private String mediaType;  // Ej: "image", "video"
    private String mediaUrl;   // src de la imagen o video
    private String title;      // Título si lo tiene (opcional)

    private List<Comment> comments;
    private List<Reaction> reactions;
}
