package com.marcicomentariosfacebook.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.model.Reaction;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 *
 * */
@AllArgsConstructor
@Component
@Slf4j
public class CommentWebSocketHandler implements WebSocketHandler {

    private final Map<String, Sinks.Many<String>> commentSinks = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<String>> reactionSinks = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<String>> postSinks = new ConcurrentHashMap<>();
    private final Map<String, Sinks.Many<String>> fromSinks = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession session) {
        // Para simplificar, enviamos TODO tipo de eventos a todos los clientes.
        // En producción, puedes hacer suscripciones filtradas.

        // Un solo sink que mezcla todos tipos
        Sinks.Many<String> combinedSink = Sinks.many().multicast().onBackpressureBuffer();

        // Registramos los sinks para enviar a combinedSink
        commentSinks.values().forEach(sink -> sink.asFlux().subscribe(combinedSink::tryEmitNext));
        reactionSinks.values().forEach(sink -> sink.asFlux().subscribe(combinedSink::tryEmitNext));
        postSinks.values().forEach(sink -> sink.asFlux().subscribe(combinedSink::tryEmitNext));
        fromSinks.values().forEach(sink -> sink.asFlux().subscribe(combinedSink::tryEmitNext));

        Flux<WebSocketMessage> outgoingMessages = combinedSink.asFlux()
                .map(session::textMessage);

        // Por simplicidad, no procesamos mensajes recibidos del cliente
        return session.send(outgoingMessages);
    }

    // Métodos para publicar cada tipo (envía JSON con campo "type" para identificar)

    public Mono<Void> publishComment(Comment comment) {
        return publishGeneric("comment", comment);
    }

    public Mono<Void> publishReaction(Reaction reaction) {
        return publishGeneric("reaction", reaction);
    }

    public Mono<Void> publishPost(Post post) {
        return publishGeneric("post", post);
    }

    public Mono<Void> publishFrom(From from) {
        return publishGeneric("from", from);
    }

    private Mono<Void> publishGeneric(String type, Object data) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("type", type);
            root.set("data", objectMapper.valueToTree(data));
            String json = objectMapper.writeValueAsString(root);

            // Envío a todos sinks de ese tipo (puedes mejorar según tu lógica)
            Map<String, Sinks.Many<String>> targetMap = switch (type) {
                case "comment" -> commentSinks;
                case "reaction" -> reactionSinks;
                case "post" -> postSinks;
                case "from" -> fromSinks;
                default -> null;
            };

            if (targetMap == null) {
                log.error("Tipo desconocido para publicar: {}", type);
                return Mono.empty();
            }

            // Si no hay sinks aún, creamos uno para broadcast
            if (targetMap.isEmpty()) {
                targetMap.put("all", Sinks.many().multicast().onBackpressureBuffer());
            }

            targetMap.values().forEach(sink -> sink.tryEmitNext(json));
            log.info("🔔 Notificando nuevo evento WebSocket Tipo: [{}], Contenido: {}", type, json);
        } catch (JsonProcessingException e) {
            log.error("Error serializando evento WS tipo {}", type, e);
        }
        return Mono.empty();
    }
}
