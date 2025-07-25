package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.services.EventoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentEventoHandler implements EventoHandler {

    private final APIGraphService apiGraphService;
    private final Environment environment;
    @Override
    public Mono<Void> manejar(String verb, WebhookPayload.Value value) {
        String page_id = environment.getProperty("meta.api.id.page");

        switch (verb.toLowerCase()) {
            case "add":
                // Evitar responder si el comentario fue hecho por la misma página para evitar bucle
                // Evitar responder si el comentario no viene con texto

                if (page_id.equals(value.getFrom().getId()) || value.getMessage() == null) {
                    return Mono.empty();
                }

                log.info("[{}] Comentario nuevo: {}", verb.toUpperCase(), value);

                log.info("mensaje recibidooooo: {}", value.getMessage());
                return apiGraphService.replyComment(value.getComment_id(), "Respuesta a comentario desde automatico")
                        .doOnNext(response -> log.info("✅ ID comentario réplica: {}", response))
                        .doOnError(e -> log.error("❌ Error respondiendo comentario", e))
                        .then();

            case "edited":
                log.info("[{}] Comentario editado: {}", verb.toUpperCase(), value);
                return Mono.empty();

            case "remove":
                log.info("[{}] Comentario eliminado: {}", verb.toUpperCase(), value);
                return Mono.empty();

            default:
                log.warn("[{}] Acción no manejada para comentario: {}", verb.toUpperCase(), value);
                return Mono.empty();
        }
    }

}
