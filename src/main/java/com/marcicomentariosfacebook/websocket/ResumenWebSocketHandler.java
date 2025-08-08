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
import java.util.List;

@AllArgsConstructor
@Component
@Slf4j
public class ResumenWebSocketHandler implements WebSocketHandler {

    private final VistaResumenService vistaResumenService;

    private final Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        log.info("Cliente conectado: {}", session.getId());

        // Enviar datos actuales al conectarse (una sola vez)
        Mono<Void> sendInitial = vistaResumenService.findAllResumen()
                .collectList()
                .flatMap(list -> {
                    try {
                        String json = mapper.writeValueAsString(list);
                        return session.send(Mono.just(session.textMessage(json)));
                    } catch (Exception e) {
                        log.error("Error serializando resúmenes iniciales", e);
                        return Mono.empty();
                    }
                });

        // Flux para enviar mensajes en tiempo real (desde el sink)
        Flux<WebSocketMessage> sendUpdates = sink.asFlux()
                .map(session::textMessage);

        // Enviar primero los datos iniciales y luego las actualizaciones continuas
        Mono<Void> sendAll = session.send(Flux.concat(sendInitial.thenMany(sendUpdates)));

        // Recibir mensajes para logging, no se cierra la conexión por esto
        Mono<Void> receive = session.receive()
                .doOnNext(msg -> log.debug("Mensaje recibido de cliente {}: {}", session.getId(), msg.getPayloadAsText()))
                .then();

        // Log cuando la conexión finalice (por cualquier motivo)
        return Mono.when(sendAll, receive)
                .doFinally(signalType -> log.info("Cliente desconectado: {}, motivo: {}", session.getId(), signalType));
    }

    // Método para enviar datos a todos los clientes conectados
    public Mono<Void> emitirResumen(List<VistaResumen> resumenes) {
        try {
            String json = mapper.writeValueAsString(resumenes);
            sink.tryEmitNext(json);
            return Mono.empty();
        } catch (Exception e) {
            log.error("Error enviando resumen", e);
            return Mono.error(e);
        }
    }
}
