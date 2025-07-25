package com.marcicomentariosfacebook.client.FACEBOOK.services;

import com.marcicomentariosfacebook.client.FACEBOOK.models.CommentsReactionsData;
import com.marcicomentariosfacebook.client.FACEBOOK.models.CommentsReactions;
import com.marcicomentariosfacebook.client.FACEBOOK.models.FacebookApiResponse;
import com.marcicomentariosfacebook.maper.FacebookMapperAttachments;
import com.marcicomentariosfacebook.maper.FacebookMapperPosts;
import com.marcicomentariosfacebook.models.Attachment;
import com.marcicomentariosfacebook.models.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class APIGraphServiceImp implements APIGraphService{

    private final WebClient.Builder webClient;
    private final Environment env;
    private final FacebookMapperPosts facebookMapper;
    private final FacebookMapperAttachments facebookMapperAttachments;

    @Override
    public Flux<Post> getPosts() {
        String URL = env.getProperty("meta.api.get.posts");
        String TOKEN = env.getProperty("meta.api.bearer.token");

        return webClient
                .build()
                .get()
                .uri(URL)
                .header("Authorization", "Bearer " + TOKEN)
                .retrieve()
                .bodyToMono(FacebookApiResponse.class)
                .flatMapMany(response -> Flux.fromIterable(facebookMapper.toPostList(response)))
                // Ahora enriquecemos attachments con comentarios y reacciones
                .flatMap(post -> {
                    // Obtener lista de attachments
                    List<Attachment> attachments = post.getAttachments();
                    if (attachments == null || attachments.isEmpty()) {
                        // Si no tiene attachments, simplemente devuelve el post
                        return Mono.just(post);
                    }

                    // Para cada attachment llamar a addCommentsAndReactionsToAttachment (retorna Mono<Attachment>)
                    // y luego recolectar todo en una lista Mono<List<Attachment>>
                    return Flux.fromIterable(attachments)
                            .flatMap(attachment -> addCommentsAndReactionsToAttachment(attachment))
                            .collectList()
                            .map(enrichedAttachments -> {
                                post.setAttachments(enrichedAttachments);
                                return post;
                            });
                })
                .onErrorResume(e -> {
                    log.error("❌ Error al intentar obtener publicaciones", e);
                    return Flux.empty();
                });
    }


    // OBTENER Y AGREGAR COMENTARIOS y REACCIONES A UN ATTACHMENT (IMAGENES, VIDEOS , ETC)
    @Override
    public Mono<Attachment> addCommentsAndReactionsToAttachment(Attachment attachment) {
        if (attachment == null || attachment.getId() == null) {
            return Mono.just(attachment);
        }

        String url = env.getProperty("facebook.api.comments.reacctions.byAttachment")
                .replace("{attachment_id}", attachment.getId());

        String TOKEN = env.getProperty("meta.api.bearer.token");

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


    @Override
    public Mono<String> sendComment(String postId, String message) {
        String url = env.getProperty("meta.api.send.comment").replace("{post_id}", postId);
        String token = env.getProperty("meta.api.bearer.token");

        Map<String, String> body = Map.of("message", message);

        return webClient.build()
                .post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("📝 Comentario enviado al post: {}", response))
                .doOnError(e -> log.error("❌ Error enviando comentario al post", e));
    }

    @Override
    public Mono<String> replyComment(String commentId, String message) {
        String url = env.getProperty("meta.api.reply.comment").replace("{comment_id}", commentId);
        String token = env.getProperty("meta.api.bearer.token");

        Map<String, String> body = Map.of("message", message);

        return webClient.build()
                .post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(response -> log.info("↪️ Respuesta enviada al comentario: {}", response))
                .doOnError(e -> log.error("❌ Error respondiendo comentario", e));
    }

}
