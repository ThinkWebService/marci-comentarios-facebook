package com.marcicomentariosfacebook.client.LHIA.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestQuestionLhia {
	private String message;
	private String usuario;
	private String descripcion;
	private String identificador;
	private String idPlatform;
	private String platform;
}