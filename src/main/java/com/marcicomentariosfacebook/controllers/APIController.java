package com.marcicomentariosfacebook.controllers;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.model.*;
import com.marcicomentariosfacebook.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final PageService pageService;
    private final ReactionService reactionService;
    private final FromService fromService;

    @GetMapping("/page")
    public Mono<Page> getPages() {
        return pageService.getMyPage();
    }

    @GetMapping("/posts")
    public Flux<Post> getPosts() {
        return postService.findAll();
    }

    @GetMapping("/comments")
    public Flux<Comment> getComments() {
        return commentService.findAll();
    }

    @GetMapping("/reactions")
    public Flux<Reaction> getReactions() {
        return reactionService.findAll();
    }

    @GetMapping("/froms")
    public Flux<From> getFroms() {
        return fromService.findAll();
    }

    // Comentar una publicación de FACEBOOK
    @PostMapping("/comments/send/{post_id}")
    public Mono<ResponseEntity<String>> sendComment(@PathVariable String post_id, @Valid @RequestBody CommentRequest request) {
        return apiGraphService.sendComment(post_id, request.getMessage())
                .map(ResponseEntity::ok);
    }

    // Responder a un comentario de FACEBOOK
    @PostMapping("/comments/reply/{comment_id}")
    public Mono<ResponseEntity<Comment>> replyComment(@PathVariable String comment_id, @Valid @RequestBody CommentRequest request) {
        return commentService.responderComentarioManual(comment_id, request)
                .map(ResponseEntity::ok);
    }

}