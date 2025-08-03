package com.marcicomentariosfacebook.controllers;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.services.CommentService;
import com.marcicomentariosfacebook.services.PostService;
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
@RequestMapping("/acf")//ApiCommentsFacebook
@RequiredArgsConstructor
public class APIController {

    private final APIGraphService apiGraphService;
    private final PostService postService;
    private final CommentService commentService;

    // Obtener publicaciones de la bd
    @GetMapping("/posts")
    public Flux<Post> getPosts() {
        return postService.findAll()
                .doOnError(e -> log.error("Error obteniendo posts", e));
    }

    // Obtener comentarios de la bd
    @GetMapping("/comments")
    public Flux<Comment> getComments() {
        return commentService.findAll()
                .doOnError(e -> log.error("Error obteniendo comentarios", e));
    }

    // Comentar una plublicacion de META
    @PostMapping("/comments/send/{post_id}")
    public Mono<ResponseEntity<String>> sendComment(@PathVariable String post_id, @Valid @RequestBody CommentRequest request) {

        return apiGraphService.sendComment(post_id, request.getMessage())
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error enviando comentario", e))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error enviando comentario"));
    }

    // Responder a un comentario de META
    @PostMapping("/comments/reply/{comment_id}")
    public Mono<ResponseEntity<String>> replyComment(@PathVariable String comment_id, @Valid @RequestBody CommentRequest request) {

        return apiGraphService.replyComment(comment_id, request.getMessage())
                .map(ResponseEntity::ok)
                .doOnError(e -> log.error("Error respondiendo comentario", e))
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error respondiendo comentario"));
    }
}