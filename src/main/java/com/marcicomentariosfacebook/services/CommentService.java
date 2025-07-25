package com.marcicomentariosfacebook.services;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import reactor.core.publisher.Mono;

public interface CommentService {

    Mono<Boolean> isAutoResponse (String id_post);
    Mono<String> reply_comment(String id_comment);
}