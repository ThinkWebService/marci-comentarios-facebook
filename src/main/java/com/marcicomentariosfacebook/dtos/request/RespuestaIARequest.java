package com.marcicomentariosfacebook.dtos.request;

import com.marcicomentariosfacebook.model.ResponseType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespuestaIARequest {
    @NotNull(message = "El campo 'response_type' es requerido")
    private ResponseType response_type;

    @NotBlank(message = "El campo 'context' es requerido")
    private String context;

    //@NotBlank(message = "El campo 'agent_user' es requerido")
    //@Email(message = "El campo 'agent_user' debe ser un correo válido")
    //private String agent_user;
}