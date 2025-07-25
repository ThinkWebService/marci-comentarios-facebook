package com.marcicomentariosfacebook.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PostRequest {

    @NotBlank(message = "El campo 'message' es requerido")
    private String message;
}