package com.marcicomentariosfacebook.client.LHIA.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcicomentariosfacebook.client.LHIA.models.AuthResponse;
import com.marcicomentariosfacebook.client.LHIA.models.RequestQuestionLhia;
import com.marcicomentariosfacebook.client.LHIA.models.ResponseLhia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Data
@Slf4j
@Service
@AllArgsConstructor
public class ApiLhia implements ApiLhiaService {

	private final WebClient.Builder webClient;
	private final Environment env;
	private final ObjectMapper objectMapper;

	@Override
	public Mono<AuthResponse> getToken() {
		String urlToken = env.getProperty("lhia.token");
		return webClient.build()
				.post()
				.uri(urlToken)
				.retrieve()
				.bodyToMono(AuthResponse.class);
	}

	@Override
	public Mono<ResponseLhia> sendMesssageToLhia(RequestQuestionLhia data, String token) {
		String urlRespuesta = env.getProperty("lhia.respuesta");
		log.info("Enviando a LHIA: {}", data);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		Map<String, Object> body = objectMapper.convertValue(data, Map.class);

		return webClient.build()
				.post()
				.uri(urlRespuesta)
				.headers(h -> h.addAll(headers))
				.bodyValue(body)
				.retrieve()
				.bodyToMono(ResponseLhia.class)
				.doOnNext(resp -> log.info("Respuesta de LHIA: {}", resp));
	}
}
