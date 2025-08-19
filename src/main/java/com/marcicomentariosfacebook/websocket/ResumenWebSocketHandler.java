package com.marcicomentariosfacebook.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.marcicomentariosfacebook.model.VistaResumen;
import com.marcicomentariosfacebook.services.VistaResumenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
@Component
@Slf4j
public class ResumenWebSocketHandler implements WebSocketHandler {

    private final VistaResumenService vistaResumenService;

    // 🔹 Un sink por cliente
    private final Map<String, Sinks.Many<String>> sessionSinks = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String sessionId = session.getId();
        log.info("✅ Cliente {} empezó a recibir actualizaciones de resúmenes", sessionId);

        // Sink exclusivo para este cliente
        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();
        sessionSinks.put(sessionId, sink);

        // 🔹 Al conectar, mandar el resumen inicial
        Mono<Void> sendInitial = vistaResumenService.findAllResumen()
                .collectList()
                .flatMap(list -> {
                    try {
                        String json = mapper.writeValueAsString(list);
                        sink.tryEmitNext(json);
                    } catch (Exception e) {
                        log.error("Error serializando resúmenes iniciales", e);
                    }
                    return Mono.empty();
                });

        // 🔹 Mantener actualizaciones + keepAlive
        Flux<WebSocketMessage> outgoing = sink.asFlux()
                .mergeWith(Flux.interval(Duration.ofSeconds(30))
                        .map(t -> "{\"type\":\"ping\"}"))
                .map(session::textMessage);

        // 🔹 Limpieza cuando se desconecta
        Mono<Void> input = session.receive()
                .doFinally(signal -> {
                    sessionSinks.remove(sessionId);
                    log.info("❌ Cliente {} dejó de recibir actualizaciones de resúmenes", sessionId);
                })
                .then();

        return sendInitial.then(session.send(outgoing).and(input));
    }

    // Método para enviar actualizaciones a TODOS los clientes conectados
    public Mono<Void> emitirResumen(List<VistaResumen> resumenes) {
        try {
            String json = mapper.writeValueAsString(resumenes);
            sessionSinks.values().forEach(sink -> sink.tryEmitNext(json));
            //log.info("📢 Enviando resumen a {} clientes", sessionSinks.size());
            return Mono.empty();
        } catch (Exception e) {
            log.error("Error enviando resumen", e);
            return Mono.error(e);
        }
    }
}
