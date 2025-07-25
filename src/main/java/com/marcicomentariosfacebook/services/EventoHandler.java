package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import reactor.core.publisher.Mono;

public interface EventoHandler {
    Mono<Void> manejar(String verb, WebhookPayload.Value value);
}