package com.marcicomentariosfacebook.client.LHIA.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcicomentariosfacebook.client.LHIA.TokenProperties;
import com.marcicomentariosfacebook.client.LHIA.dto.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@AllArgsConstructor
public class ApiMejora implements MejoraService {

	private final WebClient.Builder webClient;
	private final Environment env;
	private final ObjectMapper objectMapper;
	private final TokenProperties tokenProperties;
	private final ApiLhiaService apiLhiaService;

	@Override
	public Flux<String> sendMesssageToMejora(String message) {
		String urlRespuesta = env.getProperty("mejora.request");
		log.info("Enviando mensaje a MEJORA: {}", message);

		// Construimos los parámetros form-data
		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("message", message);
		formData.add("uuidConversation", "326478265873654");

		return apiLhiaService.getToken().flatMapMany(token -> {
			if (token == null || token.isEmpty()) {
				log.warn("Token vacío, no se enviará el mensaje a MEJORA.");
				return Flux.empty();
			}

			return webClient.build()
					.post()
					.uri(urlRespuesta)
					.headers(h -> {
						h.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
						h.setBearerAuth(token);
					})
					.body(BodyInserters.fromFormData(formData))
					.retrieve()
					.bodyToMono(ResponseQuestionLhia.class)
					.flatMapMany(response -> {
						if (response.getSuggestions() == null) {
							return Flux.empty();
						}
						return Flux.fromIterable(response.getSuggestions());
					})
					.onErrorResume(e -> {
						log.error("Error al enviar mensaje a MEJORA: {}", e.getMessage());
						return Flux.empty();
					});
		});
	}
}
