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
@Table("plantilla")
public class Plantilla {
    @Id
    private String id;
    private String name;
    private String descripcion;
    private String enlace;
    private PlantillaType plantilla_type;
}