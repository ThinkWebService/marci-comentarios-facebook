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
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Component
@Slf4j
public class CommentWebSocketHandler implements WebSocketHandler {

    // 🔹 Sinks por cliente
    private final Map<String, Sinks.Many<String>> sessionSinks = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        log.info("✅ Cliente {} empezó a recibir actualizaciones de comentarios", sessionId);

        // Sink exclusivo para este cliente
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        sessionSinks.put(sessionId, sink);

        Flux<WebSocketMessage> outgoing = sink.asFlux()
                .mergeWith(Flux.interval(Duration.ofSeconds(30))
                        .map(tick -> "{\"type\":\"ping\"}"))
                .map(session::textMessage);

        Mono<Void> input = session.receive()
                .doFinally(signal -> {
                    sessionSinks.remove(sessionId);
                    log.info("❌ Cliente {} dejó de recibir actualizaciones de comentarios", sessionId);
                })
                .then();

        return session.send(outgoing).and(input);
    }

    // ---------------- Métodos de publicación ---------------- //
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

            sessionSinks.values().forEach(sink -> sink.tryEmitNext(json));

            log.info("📢 Notificando [{}] a {} clientes", type, sessionSinks.size());

        } catch (JsonProcessingException e) {
            log.error("Error serializando evento WS tipo {}", type, e);
        }
        return Mono.empty();
    }
}
