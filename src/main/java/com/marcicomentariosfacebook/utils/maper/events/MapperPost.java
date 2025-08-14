package com.marcicomentariosfacebook.utils.maper.events;

import com.marcicomentariosfacebook.dtos.WebhookPayload;
import com.marcicomentariosfacebook.model.Post;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MapperPost {

    /**
     * Mapea un WebhookPayload.Value a un Post listo para guardar en BD.
     */
    public Mono<Post> mapValueToPost(WebhookPayload.Value value) {
        if (value == null) {
            return Mono.empty();
        }

        // Construir URL de Facebook si no hay link
        String postId = value.getPost_id() != null ? value.getPost_id()
                : (value.getPost() != null ? value.getPost().getId() : null);
        String fbPostUrl = postId != null ? "https://www.facebook.com/" + postId.replace("_", "/posts/") : null;

        // Obtener full_picture: photos[0] > link > fbPostUrl
        String fullPicture = null;
        if (value.getPhotos() != null && !value.getPhotos().isEmpty()) {
            fullPicture = value.getPhotos().get(0);
        } else if (value.getLink() != null) {
            fullPicture = value.getLink();
        } else {
            fullPicture = fbPostUrl;
        }

        // permalink_url: preferimos link, sino construimos URL de FB
        String permalinkUrl = value.getLink() != null ? value.getLink() : fbPostUrl;

        // Construir status concatenando verb + item
        String status = null;
        if (value.getVerb() != null && value.getItem() != null) {
            switch (value.getVerb()) {
                case "add":
                    status = "added_" + value.getItem();
                    break;
                case "edited":
                    status = "edited_" + value.getItem();
                    break;
                case "remove":
                    status = "removed_" + value.getItem();
                    break;
                default:
                    status = value.getVerb() + "_" + value.getItem(); // fallback
            }
        }

        Post post = Post.builder()
                .id(postId)
                .message(value.getMessage())
                .full_picture(fullPicture)
                .permalink_url(permalinkUrl)
                .status_type(value.getPost() != null ? value.getPost().getStatus_type() : null)
                .story(value.getPost() != null ? value.getPost().getPromotion_status() : null)
                .published(value.getIsPublished() != null ? value.getIsPublished()
                        : (value.getPublished() != null && value.getPublished() == 1))
                .created_time(value.getCreated_time())
                .updated_time(value.getUpdated_time())
                .verb(value.getVerb())
                .status_type(status) // <--- nuevo campo
                .page_id(value.getFrom() != null ? value.getFrom().getId() : null)
                .build();

        return Mono.just(post);
    }
}
