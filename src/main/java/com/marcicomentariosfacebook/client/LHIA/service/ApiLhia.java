package com.marcicomentariosfacebook.client.LHIA.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcicomentariosfacebook.client.LHIA.TokenProperties;
import com.marcicomentariosfacebook.client.LHIA.dto.AuthResponse;
import com.marcicomentariosfacebook.client.LHIA.dto.RequestQuestionLhia;
import com.marcicomentariosfacebook.client.LHIA.dto.ResponseLhia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Data
@Slf4j
@Service
@AllArgsConstructor
public class ApiLhia implements ApiLhiaService {

	private final WebClient.Builder webClient;
	private final Environment env;
	private final ObjectMapper objectMapper;
	private final TokenProperties tokenProperties;

	@Override
	public Mono<String> getToken() {
		return webClient.build()
				.post()
				.uri(tokenProperties.getUrl())
				.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
				.body(BodyInserters.fromFormData("username", tokenProperties.getUsername())
						.with("password", tokenProperties.getPassword())
						.with("client_id", tokenProperties.getClient_id())
						.with("grant_type", tokenProperties.getGrant_type()))
				.retrieve()
				.bodyToMono(AuthResponse.class)
				.map(AuthResponse::getAccess_token)
				.onErrorResume(e -> {
					log.error("Error al obtener token: {}", e.getMessage());
					return Mono.empty();
				});
	}


	@Override
	public Mono<String> sendMesssageToLhia(String message) {
		String urlRespuesta = env.getProperty("lhia.request.url");
		log.info("Pregunta a LHIA: {}", message);

		// Crear objeto de request local
		RequestQuestionLhia rql = new RequestQuestionLhia();
		rql.setQuestion(message);

		return getToken().flatMap(token -> {
			if (token == null || token.isEmpty()) {
				log.warn("Token vacío, no se enviará el mensaje a LHIA.");
				return Mono.empty();
			}

			return webClient.build()
					.post()
					.uri(urlRespuesta)
					.headers(h -> {
						h.setContentType(MediaType.APPLICATION_JSON);
						h.setBearerAuth(token);
					})
					.bodyValue(rql)
					.retrieve()
					.bodyToMono(ResponseLhia.class)
					.map(ResponseLhia::getRespuesta)
					.flatMap(res -> {
						if (res == null || res.isBlank()) {
							log.info("LHIA NO encontró una respuesta");
							return Mono.empty();
						}
						//log.info("Respuesta de LHIA: {}", message);
						return Mono.just(res);
					})
					.onErrorResume(e -> {
						log.error("Error al enviar mensaje a LHIA: {}", e.getMessage());
						return Mono.empty();
					});
		});
	}
}