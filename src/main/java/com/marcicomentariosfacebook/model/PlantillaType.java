package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table("plantilla_type")
public class  PlantillaType {
    @Id
    private Long id;       // UUID o secuencia
    private String nombre;   // Ej: "SALUDO", "AGRADECIMIENTO", etc
    private String descripcion; // Opcional: explicación del tipo
    @CreatedDate
    @Column("create_time")
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column("update_time")
    private LocalDateTime updateTime;
}