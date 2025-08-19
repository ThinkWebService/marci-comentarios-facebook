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

        // Mono para guardar el From (usuario) si existe y asegurar que esté en BD antes de continuar
        Mono<From> fuserMono = Mono.justOrEmpty(value.getFrom())
                .filter(from -> from.getId() != null)
                .map(from -> From.builder()
                        .id(from.getId())
                        .name(from.getName())
                        .build())
                .flatMap(fromService::save)
                // En caso de no haber usuario, emitimos vacío, lo manejaremos luego
                .switchIfEmpty(Mono.empty());

        return mapperComment.mapValueToComment(value)
                .switchIfEmpty(Mono.fromRunnable(() ->
                        log.warn("No se pudo mapear comentario desde value: {}", value)
                ))
                .flatMap(comment ->
                        // Esperamos a que fuserMono complete (con o sin usuario)
                        fuserMono
                                .switchIfEmpty(Mono.just(From.builder().id(null).name(null).build())) // Emite un From "vacío" para no usar null
                                .flatMap(from -> {
                                    switch (verb) {
                                        case "add":
                                            // Aquí pasamos solo comment y pageId porque fuserMono ya consumido
                                            return manejarAdd(comment, pageId);
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
                );
    }

    // Cambié para no pasar fuserMono porque ya guardamos al usuario antes
    private Mono<Void> manejarAdd(Comment comment, String pageId) {
        Mono<Comment> commentSaved;

        if (comment.getFrom_id() != null && comment.getFrom_id().equals(pageId)) {
            // Comentario del administrador
            commentSaved = commentService.findById(comment.getId())
                    .flatMap(existingComment -> {
                        comment.setAuto_answered(existingComment.getAuto_answered());

                        if (existingComment.getAgent_user() != null) {
                            comment.setAgent_user(existingComment.getAgent_user());
                        }

                        if (existingComment.getResponse_type() != null) {
                            comment.setResponse_type(existingComment.getResponse_type());
                        }

                        log.info("Nuevo comentario de [ADMIN PAGE] desde (LHIA-MIND) a guardar o actualizar: {}", comment);
                        return commentService.save(comment);
                    })
                    .switchIfEmpty(
                            Mono.defer(() -> {
                                comment.setResponse_type(ResponseType.FACEBOOK);
                                log.info("Nuevo comentario de [ADMIN PAGE] desde (FACEBOOK) a guardar o actualizar: {}", comment);
                                return commentService.save(comment);
                            })
                    );
        } else {
            // Comentario de cliente
            log.info("Nuevo comentario de [USUARIO] desde (FACEBOOK) a guardar o actualizar: {}", comment);

            // NO se asigna agent_user automáticamente aquí
            if (comment.getResponse_type() == null) {
                comment.setResponse_type(ResponseType.FACEBOOK);
            }
            commentSaved = commentService.save(comment);
        }


        return commentSaved.flatMap(savedComment ->
                commentWebSocketHandler.publishComment(savedComment)
                        .then(Mono.defer(() -> {
                            boolean esCliente = savedComment.getFrom_id() != null
                                    && !savedComment.getFrom_id().equals(pageId)
                                    && savedComment.getMessage() != null;

                            if (!esCliente) {
                                return Mono.empty();
                            }

                            // Verificar que el post exista y tenga auto_answered = true
                            return postService.findById(savedComment.getPostId())
                                    .filter(Post::isAuto_answered) // Solo si está habilitado autorespuesta
                                    .flatMap(post -> commentService.responderComentarioAutomatico(
                                            savedComment.getId(),
                                            savedComment.getMessage()
                                    ))
                                    .switchIfEmpty(Mono.fromRunnable(() ->
                                            log.info("⛔ No se auto-responde el comentario [{}] porque el post [{}] no tiene auto_answered=true",
                                                    savedComment.getId(), savedComment.getPostId())
                                    ));
                        }))
        );
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
                .flatMap(child -> collectAllDescendants(child)) // recursión
                .startWith(parent); // incluye el comentario actual (padre)
    }
}