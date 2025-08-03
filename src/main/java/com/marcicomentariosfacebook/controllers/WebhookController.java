package com.marcicomentariosfacebook.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.services.EventoHandler;
import com.marcicomentariosfacebook.services.factory.EventoHandlerFactory;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@Data
@Slf4j
@RestController
@RequestMapping("/webhook")
@AllArgsConstructor
public class WebhookController {

    private final Environment env;
    private final EventoHandlerFactory eventoHandlerFactory;

    // ✅ Suscripción automática al iniciar la app
    @PostConstruct
    public void subscribeWebhook() {
        String pageId = env.getProperty("facebook.api.id.page");
        String accessToken = env.getProperty("facebook.api.bearer.token");

        String url = "https://graph.facebook.com/v23.0/" + pageId + "/subscribed_apps"
                + "?subscribed_fields=feed"
                + "&access_token=" + accessToken;

        log.info("⏳ Suscribiendo a la página [{}] al webhook: {} ...", pageId, url);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            log.info("✅ Suscripción completada exitosamente para la página [{}]. Respuesta: {}", pageId, response.getBody());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                log.error("❌ Error al suscribirse: token inválido o expirado. Detalle: {}", e.getResponseBodyAsString());
            } else {
                log.error("❌ Error HTTP al suscribirse: {}", e.getStatusCode(), e);
            }
        } catch (Exception e) {
            log.error("❌ Error inesperado al suscribirse", e);
        }
    }

    @GetMapping
    public Mono<ResponseEntity<String>> verifyWebhook(
            @RequestParam(name = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        log.info("🛡️ Verificando webhook con token: {} y challenge: {}", verifyToken, challenge);
        String expectedToken = env.getProperty("facebook.api.verify.token");
        if (expectedToken != null && expectedToken.equals(verifyToken)) {
            return Mono.just(ResponseEntity.ok(challenge));
        } else {
            log.error("🚫 Token de verificación inválido");
            return Mono.just(ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error de verificación"));
        }
    }


    @PostMapping
    public Mono<ResponseEntity<String>> receiveWebhook(@RequestBody Mono<String> rawBodyMono) {
        return rawBodyMono
                .doOnNext(json -> log.info("📥 JSON recibido: {}", json)) // Imprime el JSON sin parsear
                .flatMap(json -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        WebhookPayload payload = mapper.readValue(json, WebhookPayload.class);

                        for (WebhookPayload.Entry entry : payload.getEntry()) {
                            for (WebhookPayload.Change change : entry.getChanges()) {
                                if (!"feed".equals(change.getField())) continue;

                                WebhookPayload.Value value = change.getValue();

                                String item = value.getItem();
                                String verb = value.getVerb();

                                log.info("📌 Evento recibido: item={}, verb={}", item, verb);

                                EventoHandler handler = eventoHandlerFactory.getHandler(item);
                                if (handler != null) {
                                    handler.manejar(verb, value).subscribe();
                                } else {
                                    log.warn("Evento no manejado: {}", item);
                                }
                            }
                        }

                        return Mono.just(ResponseEntity.ok("Evento recibido y procesado"));
                    } catch (Exception e) {
                        log.error("❌ Error al deserializar JSON del webhook", e);
                        return Mono.just(ResponseEntity.badRequest().body("JSON inválido"));
                    }
                });
    }


}