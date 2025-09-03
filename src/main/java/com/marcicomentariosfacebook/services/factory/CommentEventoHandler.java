package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.model.ResponseType;
import com.marcicomentariosfacebook.services.CommentService;
import com.marcicomentariosfacebook.services.EventoHandler;
import com.marcicomentariosfacebook.services.FromService;
import com.marcicomentariosfacebook.services.PostService;
import com.marcicomentariosfacebook.utils.maper.events.MapperComment;
import com.marcicomentariosfacebook.websocket.CommentWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentEventoHandler implements EventoHandler {

    private final Environment environment;
    private final CommentWebSocketHandler commentWebSocketHandler;
    private final CommentService commentService;
    private final MapperComment mapperComment;
    private final FromService fromService;
    private final PostService postService;

    @Override
    public Mono<Void> manejar(String verb, WebhookPayload.Value value) {
        String pageId = environment.getProperty("facebook.api.id.page");

        // Guardar usuario (From) si existe
        Mono<From> fuserMono = Mono.justOrEmpty(value.getFrom())
                .filter(from -> from.getId() != null)
                .map(from -> From.builder()
                        .id(from.getId())
                        .name(from.getName())
                        .build())
                .flatMap(fromService::save)
                .switchIfEmpty(Mono.empty());

        return mapperComment.mapValueToComment(value)
                .switchIfEmpty(Mono.fromRunnable(() ->
                        log.warn("No se pudo mapear comentario desde value: {}", value)
                ))
                .flatMap(comment ->
                        fuserMono
                                .switchIfEmpty(Mono.just(From.builder().build())) // evita null
                                .flatMap(from ->
                                        // 🔑 Buscar o crear el Post una sola vez
                                        postService.findById(comment.getPostId())
                                                .switchIfEmpty(postService.savePostIfNotExist(value.getPost_id(), pageId))
                                                .flatMap(post -> {
                                                    switch (verb) {
                                                        case "add":
                                                            return manejarAdd(comment, pageId, post);
                                                        case "edited":
                                                        case "remove":
                                                        case "hide":
                                                        case "unhide":
                                                            return updateStateComment(comment, verb);
                                                        default:
                                                            log.warn("⚠️ Verb no manejado todavía: {}", verb);
                                                            return Mono.empty();
                                                    }
                                                })
                                )
                );
    }

    private Mono<Void> manejarAdd(Comment comment, String pageId, Post post) {
        Mono<Comment> commentSaved;

        if (comment.getFrom_id() != null && comment.getFrom_id().equals(pageId)) {
            // Comentario del administrador
            commentSaved = commentService.findById(comment.getId())
                    .flatMap(existingComment -> {
                        comment.setAuto_answered(existingComment.getAuto_answered());
                        comment.setAgent_user(existingComment.getAgent_user());
                        comment.setResponse_type(existingComment.getResponse_type());
                        log.info("Nuevo comentario de [ADMIN PAGE] desde (LHIA-MIND) a guardar o actualizar: {}", comment);
                        return commentService.save(comment);
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        comment.setResponse_type(ResponseType.FACEBOOK);
                        log.info("Nuevo comentario de [ADMIN PAGE] desde (FACEBOOK) a guardar o actualizar: {}", comment);
                        return commentService.save(comment);
                    }));
        } else {
            // Comentario de cliente
            log.info("Nuevo comentario de [USUARIO] desde (FACEBOOK) a guardar o actualizar: {}", comment);
            if (comment.getResponse_type() == null) {
                comment.setResponse_type(ResponseType.FACEBOOK);
            }
            commentSaved = commentService.save(comment);
        }

        return commentSaved
                .flatMap(savedComment -> commentWebSocketHandler.publishComment(savedComment))
                .then(Mono.defer(() -> {
                    // Solo responde si es cliente
                    boolean esCliente = comment.getFrom_id() != null
                            && !comment.getFrom_id().equals(pageId)
                            && comment.getMessage() != null;

                    if (!esCliente) {
                        return Mono.empty();
                    }

                    if (!post.getAuto_answered()) {
                        log.info("⛔ No se auto-responde el comentario [{}] porque el post [{}] no tiene auto_answered=true",
                                comment.getId(), post.getId());
                        return Mono.empty();
                    }

                    return commentService.responderComentarioAutomatico(
                                    comment.getId(),
                                    comment.getMessage()
                            )
                            .switchIfEmpty(Mono.fromRunnable(() ->
                                    log.warn("⚠️ No se auto-responde el comentario [{}] porque LHIA no generó respuesta", comment.getId())
                            ));
                }));
    }

    private Mono<Void> updateStateComment(Comment parentComment, String verb) {
        parentComment.setVerb(verb);

        if ("edited".equals(verb)) {
            log.info("✏️ Se actualizará SOLO el comentario [{}] con verb='edited'", parentComment.getId());
            return commentService.save(parentComment)
                    .flatMap(commentWebSocketHandler::publishComment)
                    .then();
        }

        log.info("🔁 Se actualizará el estado del comentario [{}] y todos sus descendientes a [{}]", parentComment.getId(), verb);

        return collectAllDescendants(parentComment)
                .map(comment -> {
                    comment.setVerb(verb);
                    return comment;
                })
                .collectList()
                .flatMapMany(commentService::saveAll)
                .flatMap(comment -> commentWebSocketHandler.publishComment(comment).thenReturn(comment))
                .then();
    }

    /**
     * Recursivamente obtiene todos los descendientes del comentario, incluyendo el padre.
     */
    private Flux<Comment> collectAllDescendants(Comment parent) {
        return commentService.findAllByParentId(parent.getId())
                .flatMap(this::collectAllDescendants)
                .startWith(parent);
    }
}
