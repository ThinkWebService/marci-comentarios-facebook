package com.marcicomentariosfacebook.dtos;

import com.marcicomentariosfacebook.model.ResponseType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Data
public class CommentRequest {

    @NotBlank(message = "El campo 'message' es requerido")
    private String message;

    @NotBlank(message = "El campo 'agent_user' es requerido")
    @Email(message = "El campo 'agent_user' debe ser un correo válido")
    private String agent_user;

    @NotNull(message = "El campo 'response_type' es requerido")
    private ResponseType response_type;
}