package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.services.EventoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventoHandler implements EventoHandler {

    @Override
    public Mono<Void> manejar(String verb, WebhookPayload.Value value) {
        return Mono.fromRunnable(() -> {
            switch (verb) {
                case "add":
                    log.info("[{}] Post nuevo: {}", verb.toUpperCase(), value);
                    break;
                case "edited":
                    log.info("[{}] Post editado: {}", verb.toUpperCase(), value);
                    break;
                case "remove":
                    log.info("[{}] Post eliminado: {}", verb.toUpperCase(), value);
                    break;
                default:
                    log.warn("[{}] Acción no manejada para post: {}", verb.toUpperCase(), verb);
            }
        });
    }
}
