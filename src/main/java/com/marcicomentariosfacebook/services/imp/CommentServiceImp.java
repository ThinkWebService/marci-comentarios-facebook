package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.client.LHIA.service.ApiLhiaService;
import com.marcicomentariosfacebook.dtos.CommentRequest;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.ResponseType;
import com.marcicomentariosfacebook.repositories.CommentRepository;
import com.marcicomentariosfacebook.services.CommentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class CommentServiceImp implements CommentService {

    private final CommentRepository commentRepository;
    private final APIGraphService apiGraphService;
    private final ApiLhiaService apiLhiaService;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final FromServiceImp fromServiceImp;

    @Override
    public Mono<Comment> save(Comment comment) {
        return commentRepository.findById(comment.getId())
                .map(existing -> {
                    // ----- VERB LOGIC -----
                    if (comment.getVerb() == null && existing.getVerb() == null) {
                        comment.setVerb("add");
                    } else if (comment.getVerb() == null) {
                        comment.setVerb(existing.getVerb()); // conservar el existente
                    } else if ("add".equals(comment.getVerb()) && "edited".equals(existing.getVerb())) {
                        comment.setVerb("edited"); // preservar edición previa
                    }
                    // En cualquier otro caso, se mantiene el verb nuevo tal como viene

                    // ----- RESPONSE TYPE LOGIC -----
                    if (comment.getResponse_type() == null && existing.getResponse_type() == null) {
                        comment.setResponse_type(ResponseType.FACEBOOK);
                    } else if (comment.getResponse_type() == null) {
                        comment.setResponse_type(existing.getResponse_type());
                    }

                    return existing.mergeNonNull(comment);
                })
                .flatMap(commentRepository::save)
                .switchIfEmpty(
                        Mono.defer(() -> {
                            // Si no existe en la BD, asignar valores predeterminados si son nulos
                            if (comment.getVerb() == null) {
                                comment.setVerb("add");
                            }
                            if (comment.getResponse_type() == null) {
                                comment.setResponse_type(ResponseType.FACEBOOK);
                            }
                            return r2dbcEntityTemplate.insert(Comment.class).using(comment);
                        })
                )
                // Setear el username antes de devolver el comment guardado
                .flatMap(savedComment ->
                        fromServiceImp.getUserNameByFromId(savedComment.getFrom_id())
                                .defaultIfEmpty("")  // Por si no se encuentra username
                                .map(username -> {
                                    savedComment.setFrom_name(username);
                                    return savedComment;
                                })
                );
    }

    @Override
    public Flux<Comment> saveAll(List<Comment> comments) {
        return Flux.fromIterable(comments)
                .concatMap(this::save);
    }

    @Override
    public Flux<Comment> findAllByParentId(String id) {
        return commentRepository.findByParentId(id);
    }

    @Override
    public Mono<Comment> findById(String id) {
        return commentRepository.findById(id);
    }

    @Override
    public Flux<Comment> findByPostId(String post_id) {
        return commentRepository.findByPostId(post_id);
    }

    @Override
    public Flux<Comment> findAll() {
        return commentRepository.findAll()
                .flatMap(comment ->
                        Mono.justOrEmpty(comment.getFrom_id())        // solo si from_id no es null/empty
                                .flatMap(fromServiceImp::getUserNameByFromId)
                                .defaultIfEmpty("")                        // si no hay usuario, devolvemos ""
                                .map(username -> {
                                    comment.setFrom_name(username);
                                    return comment;
                                })
                );
    }

    @Override
        public Mono<Void> responderComentarioAutomatico(String parent_id, String parent_message) {
            return apiLhiaService.sendMesssageToLhia(parent_message)
                    // 1. OBTENER RESPUESTA DE LHIA
                    .flatMap(respuestaLhia -> {
                        log.info("🧠 Respuesta de LHIA: {}", respuestaLhia);
                        return apiGraphService.replyComment(parent_id, respuestaLhia)
                                .flatMap(new_comment_id -> {
                                    // Guardar en BD el nuevo comentario
                                    Comment replyComment = Comment.builder()
                                            .id(new_comment_id)
                                            .auto_answered(true)
                                            .response_type(ResponseType.LHIA)
                                            .build();
                                    log.info("Comentario respondido automatico a guardar: {} ",replyComment);
                                    return save(replyComment);
                                })
                                .doOnNext(id -> log.info("✅ Comentario respuesta guardado en BD con id: {}", id));
                    })
                    .onErrorResume(e -> {
                        log.error("❌ Error procesando comentario con LHIA", e);
                        return Mono.empty();
                    })
                    .then();
        }

    @Override
    public Mono<Comment> responderComentarioManual(String parent_id, CommentRequest commentRequest) {
        return apiGraphService.replyComment(parent_id, commentRequest.getMessage())
                .flatMap(new_comment_id -> {
                    Comment replyComment = Comment.builder()
                            .id(new_comment_id)
                            .agent_user(commentRequest.getAgent_user())
                            .auto_answered(false)
                            .parentId(parent_id)
                            .response_type(commentRequest.getResponse_type())
                            .build();
                    log.info("📝 Comentario respondido manualmente a guardar: {}", replyComment);
                    return save(replyComment); // ← este debe retornar Mono<Comment>
                })
                .doOnNext(comment -> log.info("✅ Comentario manual guardado en BD: {}", comment))
                .onErrorResume(e -> {
                    log.error("❌ Error respondiendo comentario manual", e);
                    return Mono.empty(); // o Mono.error(e) si quieres propagar
                });
    }


    @Override
    public Mono<Comment> eliminarComentario(String comment_id, String agent_username) {
        return commentRepository.findById(comment_id)
                .flatMap(comment ->
                        apiGraphService.deleteComment(comment_id)
                                .flatMap(eliminado -> {
                                    if (Boolean.TRUE.equals(eliminado)) {
                                        comment.setVerb("remove"); // ya se actualiza con evento
                                        comment.setAgent_user(agent_username);
                                        return commentRepository.save(comment);
                                    }
                                    return Mono.empty();
                                })
                )
                .doOnError(e -> log.error("Error eliminando comentario: ", e));
    }

    // Este método elimina un comentario existente y crea uno nuevo para NOTIFICAR la edición(nueva respuesta)
    @Override
    public Mono<Comment> editarComentario(String comment_id, CommentRequest request) {
        return eliminarComentario(comment_id, request.getAgent_user())
                .flatMap(originalComment -> {
                    if (originalComment != null) {
                        return apiGraphService.replyComment(originalComment.getParentId(), request.getMessage())
                                .flatMap(new_comment_id -> {
                                    Comment editedComment = Comment.builder()
                                            .id(new_comment_id)
                                            .agent_user(request.getAgent_user())
                                            .auto_answered(false)
                                            .parentId(originalComment.getParentId())
                                            .response_type(request.getResponse_type())
                                            .verb("edited")
                                            .previousVersionId(originalComment.getId())
                                            .build();
                                    log.info("📝 Comentario editado desde LHIA-MIND guardado: {}", editedComment);
                                    return save(editedComment);
                                });
                    } else {
                        return Mono.empty();
                    }
                })
                .doOnError(e -> log.error("❌ Error editando comentario", e));
    }
}
