package com.marcicomentariosfacebook.maper;

import com.marcicomentariosfacebook.client.FACEBOOK.models.*;
import com.marcicomentariosfacebook.models.*;
import com.marcicomentariosfacebook.models.Attachment;
import com.marcicomentariosfacebook.models.Comment;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FacebookMapperPosts {


    // ------------- MAPEAR LA RESPUESTA DE META A UNA MÁS FILTRADA Y ENTENDIBLE (NO INCLUYE COMENTARIOS NI REACCIONES EN LOS ATTACHMENT)

    public List<Post> toPostList(FacebookApiResponse response) {
        if (response == null || response.getPosts() == null ||
                response.getPosts().getData() == null || response.getPosts().getData().isEmpty()) {
            return Collections.emptyList();
        }

        return response.getPosts().getData().stream()
                .map(this::mapToPost)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Post mapToPost(FacebookPost fbPost) {
        if (fbPost == null) return null;

        return Post.builder()
                .id(fbPost.getId())
                .message(fbPost.getMessage())
                .full_picture(fbPost.getFull_picture())
                .permalink_url(fbPost.getPermalink_url())
                .is_published(determinePublishedStatus(fbPost))
                .attachments(processAttachments(fbPost.getAttachments()))
                .created_time(fbPost.getCreated_time())
                .updated_time(fbPost.getCreated_time())
                .story(fbPost.getStory())
                .status_type(fbPost.getStatus_type())
                .reactions(processReactions(fbPost.getReactions()))
                .comments(processComments(fbPost.getComments()))
                .build();
    }

    private List<Attachment> processAttachments(AttachmentsWrapper attachments) {
        if (attachments == null || attachments.getData() == null) {
            return Collections.emptyList();
        }

        return attachments.getData().stream()
                .filter(Objects::nonNull)
                .flatMap(attachment ->
                        attachment.getSubattachments() != null &&
                                attachment.getSubattachments().getData() != null ?
                                attachment.getSubattachments().getData().stream() :
                                Stream.empty())
                .map(this::mapToAttachment)
                .collect(Collectors.toList());
    }

    private Attachment mapToAttachment(MediaItem mediaItem) {
        return Attachment.builder()
                .id(mediaItem.getTarget() != null ? mediaItem.getTarget().getId() : null)
                .type(mediaItem.getType())
                .url(mediaItem.getUrl())
                .mediaType(mediaItem.getMedia_type())
                .mediaUrl(resolveMediaUrl(mediaItem))
                .build();
    }

    private String resolveMediaUrl(MediaItem mediaItem) {
        if (mediaItem.getMedia() == null) return null;

        return mediaItem.getMedia().getImage() != null ?
                mediaItem.getMedia().getImage().getSrc() :
                mediaItem.getMedia().getSource();
    }

    private List<com.marcicomentariosfacebook.models.Reaction> processReactions(
            com.marcicomentariosfacebook.client.FACEBOOK.models.Reactions reactions) {

        if (reactions == null || reactions.getData() == null) {
            return Collections.emptyList();
        }

        return reactions.getData().stream()
                .map(this::mapFacebookReactionToDomain)
                .collect(Collectors.toList());
    }

    private com.marcicomentariosfacebook.models.Reaction mapFacebookReactionToDomain(
            com.marcicomentariosfacebook.client.FACEBOOK.models.Reaction fbReaction) {

        return com.marcicomentariosfacebook.models.Reaction.builder()
                .id(fbReaction.getId())
                .name(fbReaction.getName())
                .type(fbReaction.getType())
                .build();
    }

    private List<Comment> processComments(com.marcicomentariosfacebook.client.FACEBOOK.models.CommentsReactionsData comments) {
        if (comments == null || comments.getData() == null) {
            return Collections.emptyList();
        }

        return comments.getData().stream()
                .map(this::mapToComment)
                .collect(Collectors.toList());
    }

    private Comment mapToComment(CommentsReactions fbComment) {
        return Comment.builder()
                .id(fbComment.getId())
                .message(fbComment.getMessage())
                .created_time(fbComment.getCreated_time())
                .updated_time(fbComment.getUpdated_time())
                .from(mapToAuthor(fbComment.getFrom()))
                .reactions(processReactions(fbComment.getReactions()))
                .replies(fbComment.getComments() != null ?
                        processComments(fbComment.getComments()) :
                        Collections.emptyList())
                .build();
    }

    private Author mapToAuthor(User fbUser) {
        if (fbUser == null) return null;
        return new Author(fbUser.getId(), fbUser.getName());
    }

    private boolean determinePublishedStatus(FacebookPost fbPost) {
        return fbPost.getStatus_type() != null &&
                !fbPost.getStatus_type().equalsIgnoreCase("draft");
    }



}
