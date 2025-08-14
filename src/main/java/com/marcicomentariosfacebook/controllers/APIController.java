package com.marcicomentariosfacebook.controllers;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.client.LHIA.service.ApiLhiaService;
import com.marcicomentariosfacebook.client.LHIA.service.MejoraService;
import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.dtos.request.RespuestaIARequest;
import com.marcicomentariosfacebook.model.*;
import com.marcicomentariosfacebook.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@Slf4j
@RestController
@RequestMapping("/api")//ApiCommentsFacebook
@RequiredArgsConstructor
public class APIController {

    private final APIGraphService apiGraphService;
    private final PostService postService;
    private final CommentService commentService;
    private final PageService pageService;
    private final ReactionService reactionService;
    private final FromService fromService;
    private final ApiLhiaService apiLhiaService;
    private final PlantillaService plantillaService;
    private final MejoraService mejoraService;

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

    @GetMapping("/reactions/{post_id}")
    public Flux<Reaction> getReactionsByPostId(@PathVariable String post_id) {
        return reactionService.findByPostId(post_id);
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

    // Editar comentario existente en Facebook (ELIMINAR EXISTENTE RESPONDER UNO NUEVO)
    @PostMapping("/comments/edit/{comment_id}")
    public Mono<ResponseEntity<Comment>> editComment(@PathVariable String comment_id, @Valid @RequestBody CommentRequest request) {
        return commentService.editarComentario(comment_id, request)
                .map(ResponseEntity::ok);
    }

    // Eliminar comentario en Facebook
    @DeleteMapping("/comments/remove/{comment_id}")
    public Mono<ResponseEntity<Comment>> removeComment(@PathVariable String comment_id, @RequestParam String agent_user) {
        return commentService.eliminarComentario(comment_id, agent_user)
                .map(ResponseEntity::ok);
    }

    //ENDPOINTS PARA TIPOS DE SUGERENCIAS: LHIA o MEJORA o PLANTILLA
    @PostMapping("/sugerencia/lhia")
    public Mono<ResponseEntity<Map<String, String>>> getSugerenciaLHIA(@Valid @RequestBody RespuestaIARequest request) {
        return apiLhiaService.sendMesssageToLhia(request.getContext())
                .map(respuesta -> ResponseEntity.ok(Map.of("message", respuesta)));
    }

    @PostMapping("/sugerencia/mejora")
    public Mono<ResponseEntity<List<String>>> getSugerenciaMejora(@Valid @RequestBody RespuestaIARequest request) {
        return mejoraService.sendMesssageToMejora(request.getContext())
                .collectList()  // Flux<String> -> Mono<List<String>>
                .map(ResponseEntity::ok);
    }

    @GetMapping("/sugerencia/plantillas")
    public Flux<Plantilla> getPlantilla() {
        return plantillaService.findAll();
    }
}