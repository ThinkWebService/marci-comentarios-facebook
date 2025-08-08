package com.marcicomentariosfacebook.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table("vista_resumen")
public class VistaResumen {

    @Id
    @Column("id_post")
    private String id_post;

    @Column("mensaje")
    private String mensaje;

    @Column("creado")
    private LocalDateTime creado;

    @Column("comentarios")
    private Long comentarios; // Total de comentarios (todos)

    @Column("comentarios_directos")
    private Long comentarios_directos; // Comentarios sin parent_id (inicio de conversación)

    @Column("respuestas")
    private Long respuestas; // Comentarios con parent_id (respuestas)

    @Column("comentarios_respondidos")
    private Long comentarios_respondidos; // Aquellos comentarios padres que tienen al menos una respuesta

    @Column("comentarios_no_respondidos")
    private Long comentarios_no_respondidos; // Comentarios padres sin respuestas

    @Column("auto_respondidos")
    private Long auto_respondidos; // Respuestas automáticas

    @Column("agente_respondidos")
    private Long agente_respondidos; // Comentarios respondidos por agente (COMENTARIOS QUE TENGAN AUTORESPONDIDO FALSE Y RESPONSE_TYPE:  LHIA, SIMPLE, PLANTILLA, MEJORADA)

    @Column("segundos_respuesta_auto")
    private Double segundos_respuesta_auto; // Tiempo promedio respuesta automática

    //NUEVAS ---
    private Long respuestas_facebook;
    private Long respuestas_lhia; //AGENTE
    private Long respuestas_simple; //AGENTE
    private Long respuestas_plantilla; //AGENTE
    private Long respuestas_mejoradas; //AGENTE

    // NUEVAS ---
    @Column("segundos_respuesta_agente")
    private Double segundos_respuesta_agente; // Tiempo promedio respuesta de agente

    @Column("reacciones")
    private Long reacciones; // Total de reacciones

    @Column("usuarios")
    private Long usuarios; // Usuarios únicos (comentarios + reacciones)

    @Column("actualizado")
    private LocalDateTime actualizado;
}
