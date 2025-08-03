package com.marcicomentariosfacebook.services;

import reactor.core.publisher.Mono;

public interface MessageService {
    Mono<String> generateNewMessage(String message_type, String content);
}