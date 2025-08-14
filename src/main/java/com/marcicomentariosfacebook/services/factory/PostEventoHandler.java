package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.services.EventoHandler;
import com.marcicomentariosfacebook.services.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import com.marcicomentariosfacebook.utils.maper.events.MapperPost;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventoHandler implements EventoHandler {

    private final PostService postService;
    private final MapperPost mapperPost;

    @Override
    public Mono<Void> manejar(String verb, WebhookPayload.Value value) {
        switch (verb) {
            case "add":
                return manejarAdd(value);       // Guarda posts nuevos
            case "edited":
                return manejarEdited(value);    // Edita posts o elimina si no hay contenido
            default:
                log.warn("[{}] Acción no manejada para post: {}", verb.toUpperCase(), verb);
                return Mono.empty();
        }
    }

    private Mono<Void> manejarAdd(WebhookPayload.Value value) {
        log.info("[ADD] Nuevo post recibido: {}", value);

        return mapperPost.mapValueToPost(value)
                .flatMap(postService::save)   // Guardar en BD
                .then();
    }

    private Mono<Void> manejarEdited(WebhookPayload.Value value) {
        log.info("[EDITED] Post editado: {}", value);

        // Detectar post eliminado (sin contenido visible)
        boolean isDeleted = value.getMessage() == null && value.getLink() == null
                && (value.getPhotos() == null || value.getPhotos().isEmpty());

        if (isDeleted) {
            log.info("⚠️ Post aparentemente eliminado según webhook: {}", value.getPost_id());
            return postService.eliminar(value.getPost_id())  // Marcar/guardar como eliminado
                    .then();
        }

        // Post realmente editado
        return mapperPost.mapValueToPost(value)
                .flatMap(post -> postService.editar(post))
                .then();
    }
}