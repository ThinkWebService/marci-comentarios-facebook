package com.marcicomentariosfacebook.controllers;

import com.marcicomentariosfacebook.client.FACEBOOK.models.FacebookApiResponse;
import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.models.Post;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/api/facebook")
@RequiredArgsConstructor
public class APIGraphController {

    private final APIGraphService apiGraphService;

    // Obtener posts
    @GetMapping("/get_posts")
    public Flux<Post> getPosts() {
        return apiGraphService.getPosts()
                .doOnError(e -> log.error("Error obteniendo posts", e));
    }

    // Enviar comentario a un post
    @PostMapping("/send_comment/{postId}")
    public Mono<ResponseEntity<String>> sendComment(@PathVariable String postId, @Valid @RequestBody CommentRequest request) {

        return apiGraphService.sendComment(postId, request.getMessage())
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error enviando comentario", e))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error enviando comentario"));
    }

    // Responder a un comentario
    @PostMapping("/reply_comment/{commentId}")
    public Mono<ResponseEntity<String>> replyComment(@PathVariable String commentId, @Valid @RequestBody CommentRequest request) {

        return apiGraphService.replyComment(commentId, request.getMessage())
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error respondiendo comentario", e))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error respondiendo comentario"));
    }
}