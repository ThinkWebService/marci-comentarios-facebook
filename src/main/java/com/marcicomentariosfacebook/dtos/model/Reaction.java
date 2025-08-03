package com.marcicomentariosfacebook.dtos.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reaction {
    private String id;     // ID del usuario que reaccionó
    private String name;   // Nombre del usuario
    private String type;   // Tipo de reacción: LIKE, LOVE, HAHA...
}