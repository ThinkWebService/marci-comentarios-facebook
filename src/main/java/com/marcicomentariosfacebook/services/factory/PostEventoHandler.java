package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.services.EventoHandler;
import com.marcicomentariosfacebook.services.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostEventoHandler implements EventoHandler {

    private final PostService postService;
    private final APIGraphService apiGraphService;

    @Override
    public Mono<Void> manejar(String verb, WebhookPayload.Value value) {
        return switch (verb) {
            case "add", "edited" -> getPostInfoAndUpdate(value).then();
            case "remove" -> postService.eliminar(value.getPost_id()).then();
            default -> {
                log.warn("[{}] Acción no manejada para post: {}", verb.toUpperCase(), verb);
                yield Mono.empty();
            }
        };
    }

    private Mono<Post> getPostInfoAndUpdate(WebhookPayload.Value value) {
        return apiGraphService.getPostInfoFromMeta(value.getPost_id())
                .flatMap(postInfo -> {
                    // Si Facebook devolvió info, actualizar/guardar normalmente
                    Post post = new Post();
                    post.setId(postInfo.getId());
                    post.setMessage(postInfo.getMessage());
                    post.setFull_picture(postInfo.getFull_picture());
                    post.setPermalink_url(postInfo.getPermalink_url());
                    post.setCreated_time(postInfo.getCreated_time());
                    post.setUpdated_time(postInfo.getUpdated_time());
                    post.setStory(postInfo.getStory());
                    post.setStatus_type(postInfo.getStatus_type());
                    post.setPublished(postInfo.getPublished());
                    post.setPage_id(value.getFrom().getId());
                    post.setVerb(value.getVerb());

                    return postService.save(post);
                })
                .switchIfEmpty(
                        // Si el post no existe en Facebook
                        postService.findById(value.getPost_id())
                                .flatMap(existingPost -> {
                                    // marcar como eliminado en BD
                                    log.info("Marcando post como remove en BD: {}", value.getPost_id());
                                    return postService.eliminar(value.getPost_id());
                                })
                )
                .onErrorResume(e -> {
                    log.error("❌ Error obteniendo info o guardando post {}", value.getPost_id(), e);
                    return Mono.empty();
                });
    }
}
