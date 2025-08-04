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

    @Override
    public Mono<Comment> save(Comment comment) {
        return commentRepository.findById(comment.getId())
                .map(existing -> {
                    // Asignar 'add' si ambos son null
                    if (comment.getVerb() == null && existing.getVerb() == null) {
                        comment.setVerb("add");
                    }

                    // Asignar FACEBOOK si ambos son null
                    if (comment.getResponse_type() == null && existing.getResponse_type() == null) {
                        comment.setResponse_type(ResponseType.FACEBOOK);
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
                );
    }


    @Override
    public Mono<Void> saveAll(List<Comment> comments) {
        return Flux.fromIterable(comments)
                .concatMap(this::save)
                .then();
    }


    @Override
    public Mono<Comment> findById(String id) {
        return commentRepository.findById(id);
    }

    @Override
    public Flux<Comment> findAll() {
        return commentRepository.findAll();
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
                            .agent_user(commentRequest.getMessage())
                            .agent_user(commentRequest.getAgent_user())
                            .auto_answered(false)
                            .parent_id(parent_id)
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

}
