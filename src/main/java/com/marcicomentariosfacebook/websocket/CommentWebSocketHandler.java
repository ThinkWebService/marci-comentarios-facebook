package com.marcicomentariosfacebook.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcicomentariosfacebook.model.Comment;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@AllArgsConstructor
@Component
@Slf4j
public class CommentWebSocketHandler implements WebSocketHandler {

    private final Map<String, Sinks.Many<String>> sinks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession session) {
        final String[] emailHolder = new String[1];
        Set<String> subscribedPostIds = ConcurrentHashMap.newKeySet();

        // Sink para enviar eventos de estado (online/offline) a este cliente
        Sinks.Many<String> personalSink = Sinks.many().unicast().onBackpressureBuffer();

        Flux<String> inputMessages = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> {
                    if (message.startsWith("email:")) {
                        String email = message.substring(6).trim();
                        emailHolder[0] = email;
                        log.info("🟢 Cliente {} conectado", email);
                        // Enviar mensaje de estado online al cliente
                        personalSink.tryEmitNext("status:online:" + email);
                    } else if (message.startsWith("subscribe:")) {
                        String postId = message.substring(10).trim();
                        log.info("✅ Cliente {} suscrito a postId: {}", emailHolder[0], postId);
                        subscribedPostIds.add(postId);
                    }
                })
                .doOnCancel(() -> {
                    log.info("❌ Cliente {} canceló la conexión", emailHolder[0]);
                    personalSink.tryEmitNext("status:offline:" + emailHolder[0]);
                })
                .doOnTerminate(() -> {
                    log.info("🔴 Cliente {} desconectado", emailHolder[0]);
                    personalSink.tryEmitNext("status:offline:" + emailHolder[0]);
                })
                .share();

        // Flux para enviar actualizaciones de comentarios según suscripción
        Flux<WebSocketMessage> updates = Flux.defer(() -> {
            List<Flux<String>> fluxList = subscribedPostIds.stream()
                    .map(postId -> sinks.computeIfAbsent(postId,
                            key -> Sinks.many().multicast().onBackpressureBuffer()).asFlux())
                    .collect(Collectors.toList());

            if (fluxList.isEmpty()) {
                return Flux.never();
            }

            return Flux.merge(fluxList)
                    .map(session::textMessage);
        });

        // Flux para mensajes de estado personal (online/offline)
        Flux<WebSocketMessage> statusMessages = personalSink.asFlux()
                .map(session::textMessage);

        // Fusionar actualizaciones de comentarios y mensajes de estado
        Flux<WebSocketMessage> outputMessages = Flux.merge(updates, statusMessages);

        // Enviar mensajes al cliente y procesar la entrada
        return session.send(outputMessages)
                .and(inputMessages.then());
    }

    // Publica un nuevo comentario a todos los suscriptores del postId
    public Mono<Void> publish(Comment comment) {
        String postId = comment.getPost_id();

        try {
            String messageJson = objectMapper.writeValueAsString(comment);
            sinks.computeIfAbsent(postId, k -> Sinks.many().multicast().onBackpressureBuffer())
                    .tryEmitNext(messageJson);
            log.info("✅ Comentario publicado para postId {}: {}", postId, messageJson);
        } catch (JsonProcessingException e) {
            log.error("❌ Error al serializar el comentario a JSON", e);
        }

        return Mono.empty();
    }
}