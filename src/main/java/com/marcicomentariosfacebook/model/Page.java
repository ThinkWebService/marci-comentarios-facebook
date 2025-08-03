package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("page")
public class Page {

    @Id
    private String id;

    private String name;
    private String description; // Corresponde a 'about'
    private String username;

    private String category;           // Ej: "Media/news company"

    private String link;               // URL a la página
    private int fan_count;          // Total de "Me gusta"
    private int followers_count;    // Total de seguidores

    private String cover_url;          // URL de la portada
    private String profile_url;        // URL de la foto de perfil

    private boolean verified;       // Página verificada o no
}
