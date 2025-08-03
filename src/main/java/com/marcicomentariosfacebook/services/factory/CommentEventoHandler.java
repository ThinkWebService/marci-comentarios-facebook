package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.ResponseType;
import com.marcicomentariosfacebook.services.CommentService;
import com.marcicomentariosfacebook.services.EventoHandler;
import com.marcicomentariosfacebook.utils.maper.events.MapperComment;
import com.marcicomentariosfacebook.websocket.CommentWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentEventoHandler implements EventoHandler {

    private final Environment environment;
    private final CommentWebSocketHandler commentWebSocketHandler;
    private final CommentService commentService;
    private final MapperComment mapperComment;

    @Override
    public Mono<Void> manejar(String verb, WebhookPayload.Value value) {
        String pageId = environment.getProperty("facebook.api.id.page");

        // 1. Mapear comentario desde Value
        Comment comment = mapperComment.mapValueToComment(value);
        if (comment == null) {
            log.warn("No se pudo mapear comentario desde value: {}", value);
            return Mono.empty();
        }

        // 2. Verificar si el comentario lo hizo la misma página
        Mono<Comment> commentSaved;
        if (comment.getFrom_id() != null && comment.getFrom_id().equals(pageId)) {
            // Es un comentario hecho por la página (agente o autorrespuesta)
            commentSaved = commentService.findById(comment.getId())
                    .flatMap(existingComment -> {
                        // Aquí actualizas atributos adicionales antes de guardar
                        comment.setAuto_answered(existingComment.isAuto_answered());
                        comment.setAgent_user(existingComment.getAgent_user());
                        comment.setResponse_type(existingComment.getResponse_type());
                        return commentService.save(comment); // Actualiza
                    })
                    .switchIfEmpty(
                            Mono.defer(() -> {
                                // Si no existía antes, lo insertas con valores por defecto
                                comment.setResponse_type(ResponseType.FACEBOOK); // Publicado desde Facebook directamente
                                return commentService.save(comment);
                            })
                    );
        } else {
            // Comentario de un cliente o usuario (NO hecho por la página)
            commentSaved = commentService.save(comment);
        }

        // 3. Flujo completo en orden: guardar → notificar WS → responder (si aplica)
        return commentSaved.flatMap(parentComment -> {
            return commentWebSocketHandler.publish(parentComment)
                    .then(Mono.defer(() -> {
                        boolean esComentarioDeCliente = parentComment.getFrom_id() != null
                                && !parentComment.getFrom_id().equals(pageId)
                                && parentComment.getMessage() != null;

                        if (esComentarioDeCliente) {
                            return commentService.responderComentario(
                                    parentComment.getId(),
                                    parentComment.getMessage(),
                                    true,
                                    ResponseType.LHIA,
                                    null
                            );
                        } else {
                            return Mono.empty();
                        }
                    }));
        });
    }
}