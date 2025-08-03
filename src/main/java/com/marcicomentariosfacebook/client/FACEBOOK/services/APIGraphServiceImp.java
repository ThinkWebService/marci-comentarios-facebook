package com.marcicomentariosfacebook.client.FACEBOOK.services;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbCommentsReactionsResp;
import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbPageResp;
import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbPostsResp;
import com.marcicomentariosfacebook.client.FACEBOOK.models.CommentsReactionsData;
import com.marcicomentariosfacebook.maper.FacebookMapperAttachments;
import com.marcicomentariosfacebook.dtos.model.Attachment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class APIGraphServiceImp implements APIGraphService{

    private final WebClient.Builder webClient;
    private final Environment env;
    private final FacebookMapperAttachments facebookMapperAttachments;

    @Override
    public Mono<FbPageResp> getPageInfo() {
        String URL = env.getProperty("facebook.api.get.page");
        String TOKEN = env.getProperty("facebook.api.bearer.token");

        log.info("GET FACEBOOK -> PAGE: {}", URL);
        return webClient
                .build()
                .get()
                .uri(URL)
                .header("Authorization", "Bearer " + TOKEN)
                .retrieve()
                .bodyToMono(FbPageResp.class)
                .onErrorResume(e -> {
                    log.error("❌ Error al intentar obtener detalles de la pagina", e);
                    return Mono.empty();
                });
    }


    @Override
    public Mono<FbPostsResp> getPosts() {
        String URL = env.getProperty("facebook.api.get.posts.reactions");
        String TOKEN = env.getProperty("facebook.api.bearer.token");

        log.info("GET FACEBOOK -> POSTS: {}", URL);
        return webClient
                .build()
                .get()
                .uri(URL)
                .header("Authorization", "Bearer " + TOKEN)
                .retrieve()
                .bodyToMono(FbPostsResp.class)
                .onErrorResume(e -> {
                    log.error("❌ Error al intentar obtener publicaciones", e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<FbCommentsReactionsResp> getCommentsReactionsByPostId(String postId) {
        String URL = env.getProperty("facebook.api.get.comments.reactions").replace("{post_id}", postId);
        String TOKEN = env.getProperty("facebook.api.bearer.token");

        log.info("GET FACEBOOK -> COMMENTS | REACTIONS: {}", URL);
        return webClient
                .build()
                .get()
                .uri(URL)
                .header("Authorization", "Bearer " + TOKEN)
                .retrieve()
                .bodyToMono(FbCommentsReactionsResp.class)
                .onErrorResume(e -> {
                    log.error("❌ Error al intentar obtener comentarios y reacciones del post", e);
                    return Mono.empty();
                });
    }

    @Override
    public Mono<String> sendComment(String postId, String message) {
        String URL = env.getProperty("facebook.api.send.comment").replace("{post_id}", postId);
        String TOKEN = env.getProperty("facebook.api.bearer.token");

        log.info("POST FACEBOOK: {}", URL);

        Map<String, String> body = Map.of("message", message);

        return webClient.build()
                .post()
                .uri(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("📝 Comentario enviado al post: {}", response))
                .doOnError(e -> log.error("❌ Error enviando comentario al post", e));
    }

    @Override
    public Mono<String> replyComment(String commentId, String message) {
        String URL = env.getProperty("facebook.api.reply.comment").replace("{comment_id}", commentId);
        String TOKEN = env.getProperty("facebook.api.bearer.token");

        log.info("POST FACEBOOK: {}", URL);

        Map<String, String> body = Map.of("message", message);

        return webClient.build()
                .post()
                .uri(URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("↪️ Respuesta enviada al comentario: {}", response))
                .doOnError(e -> log.error("❌ Error respondiendo comentario", e));
    }

    // OBTENER Y AGREGAR COMENTARIOS y REACCIONES A UN ATTACHMENT (IMAGENES, VIDEOS , ETC)
    @Override
    public Mono<Attachment> addCommentsAndReactionsToAttachment(Attachment attachment) {
        if (attachment == null || attachment.getId() == null) {
            return Mono.just(attachment);
        }

        String url = env.getProperty("facebook.api.comments.reacctions.byAttachment")
                .replace("{attachment_id}", attachment.getId());

        String TOKEN = env.getProperty("facebook.api.bearer.token");

        return webClient.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + TOKEN)
                .retrieve()
                .bodyToMono(CommentsReactionsData.class)
                .map(response -> facebookMapperAttachments.updateAttachment(attachment, response))
                .onErrorResume(e -> {
                    log.warn("No se pudieron cargar comentarios del attachment {}", attachment.getId(), e);
                    return Mono.just(attachment); // deja el attachment sin comentarios si hay error
                });
    }
}
