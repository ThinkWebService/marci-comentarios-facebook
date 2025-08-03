package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.client.LHIA.service.ApiLhiaService;
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
        return commentRepository.existsById(comment.getId())
                .flatMap(exists -> {
                    if (exists) {
                        return commentRepository.save(comment);
                    } else {
                        return r2dbcEntityTemplate.insert(Comment.class).using(comment);
                    }
                });
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
    public Mono<Void> responderComentario(String parent_id, String parent_message, boolean auto_answered, ResponseType responseType, String agent_user) {
        return apiLhiaService.sendMesssageToLhia(parent_message)
                // 1. OBTENER RESPUESTA DE LHIA
                .flatMap(respuestaLhia -> {
                    log.info("🧠 Respuesta de LHIA: {}", respuestaLhia);
                    return apiGraphService.replyComment(parent_id, respuestaLhia)
                            .flatMap(new_comment_id -> {
                                // Guardar en BD el nuevo comentario
                                Comment replyComment = Comment.builder()
                                        .id(new_comment_id)
                                        .auto_answered(auto_answered)
                                        .response_type(responseType)
                                        .agent_user(agent_user)
                                        .build();
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
}
