package com.marcicomentariosfacebook.controllers;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.client.LHIA.service.ApiLhiaService;
import com.marcicomentariosfacebook.client.LHIA.service.MejoraService;
import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.dtos.request.RespuestaIARequest;
import com.marcicomentariosfacebook.exception.ConflictException;
import com.marcicomentariosfacebook.model.*;
import com.marcicomentariosfacebook.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@Slf4j
@RestController
@RequestMapping("api")//ApiCommentsFacebook
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
    private final PlantillaTypeService plantillaTypeService;

    @GetMapping("page")
    public Mono<Page> getPages() {
        return pageService.getMyPage();
    }

    @GetMapping("posts")
    public Flux<Post> getPosts() {
        return postService.findAll();
    }

    @GetMapping("comments")
    public Flux<Comment> getComments() {
        return commentService.findAll();
    }

    @GetMapping("reactions")
    public Flux<Reaction> getReactions() {
        return reactionService.findAll();
    }

    @GetMapping("reactions/{post_id}")
    public Flux<Reaction> getReactionsByPostId(@PathVariable String post_id) {
        return reactionService.findByPostId(post_id);
    }


    @GetMapping("froms")
    public Flux<From> getFroms() {
        return fromService.findAll();
    }

    // Comentar una publicación de FACEBOOK
    @PostMapping("comments/send/{post_id}")
    public Mono<ResponseEntity<String>> sendComment(@PathVariable String post_id, @Valid @RequestBody CommentRequest request) {
        return apiGraphService.sendComment(post_id, request.getMessage())
                .map(ResponseEntity::ok);
    }

    // Responder a un comentario de FACEBOOK
    @PostMapping("comments/reply/{comment_id}")
    public Mono<ResponseEntity<Comment>> replyComment(@PathVariable String comment_id, @Valid @RequestBody CommentRequest request) {
        return commentService.responderComentarioManual(comment_id, request)
                .map(ResponseEntity::ok);
    }

    // Editar comentario existente en Facebook (ELIMINAR EXISTENTE RESPONDER UNO NUEVO)
    @PostMapping("comments/edit/{comment_id}")
    public Mono<ResponseEntity<Comment>> editComment(@PathVariable String comment_id, @Valid @RequestBody CommentRequest request) {
        return commentService.editarComentario(comment_id, request)
                .map(ResponseEntity::ok);
    }

    // Eliminar comentario en Facebook
    @DeleteMapping("comments/remove/{comment_id}")
    public Mono<ResponseEntity<Comment>> removeComment(@PathVariable String comment_id, @RequestParam String agent_user) {
        return commentService.eliminarComentario(comment_id, agent_user)
                .map(ResponseEntity::ok);
    }

    //ENDPOINTS PARA TIPOS DE SUGERENCIAS: LHIA o MEJORA o PLANTILLA
    @PostMapping("sugerencia/lhia")
    public Mono<ResponseEntity<Map<String, String>>> getSugerenciaLHIA(@Valid @RequestBody RespuestaIARequest request) {
        return apiLhiaService.sendMesssageToLhia(request.getContext())
                .map(respuesta -> ResponseEntity.ok(Map.of("message", respuesta)));
    }

    @PostMapping("sugerencia/mejora")
    public Mono<ResponseEntity<List<String>>> getSugerenciaMejora(@Valid @RequestBody RespuestaIARequest request) {
        return mejoraService.sendMesssageToMejora(request.getContext())
                .collectList()  // Flux<String> -> Mono<List<String>>
                .map(ResponseEntity::ok);
    }

    @PostMapping("/posts/auto-answered/{post_id}")
    public Mono<ResponseEntity<Post>> updateAutoAnswered(
            @PathVariable String post_id,
            @RequestParam boolean auto_answered) {
        return postService.setAutoanswered(post_id, auto_answered)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // ---------------- PLANTILLAS ----------------

    // Obtener todas las plantillas
    @GetMapping("/plantillas")
    public Flux<Plantilla> getPlantillas() {
        return plantillaService.findAll();
    }

    // Guardar o actualizar una plantilla
    @PostMapping("/plantilla/save")
    public Mono<ResponseEntity<Plantilla>> savePlantilla(@Valid @RequestBody Plantilla plantilla) {
        return plantillaService.save(plantilla)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    // Eliminar una plantilla por ID
    @DeleteMapping("/plantilla/{id}")
    public Mono<ResponseEntity<Boolean>> deletePlantilla(@PathVariable Long id) {
        return plantillaService.deleteById(id)
                .then(Mono.just(ResponseEntity.ok(true))) // devuelve true después de eliminar
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false));
    }


    // ---------------- TIPOS DE PLANTILLA ----------------

    // Guardar o actualizar un tipo de plantilla
    @PostMapping("/plantilla-type/save")
    public Mono<ResponseEntity<PlantillaType>> savePlantillaType(@Valid @RequestBody PlantillaType plantillaType) {
        return plantillaTypeService.save(plantillaType)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    // Eliminar un tipo de plantilla por ID
    @DeleteMapping("/plantilla-type/{id}")
    public Mono<ResponseEntity<Boolean>> deletePlantillaType(@PathVariable Long id) {
        return plantillaTypeService.deleteById(id)
                .map(deleted -> ResponseEntity.ok(true))
                .onErrorResume(ConflictException.class, e ->
                        Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(false))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Obtener todos los tipos de plantilla
    @GetMapping("/plantilla-types")
    public Flux<PlantillaType> getAllPlantillaTypes() {
        return plantillaTypeService.findAll();
    }
}