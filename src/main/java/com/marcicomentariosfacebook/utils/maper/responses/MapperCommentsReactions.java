package com.marcicomentariosfacebook.utils.maper.responses;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbCommentsReactionsResp;
import com.marcicomentariosfacebook.model.Comment;
import com.marcicomentariosfacebook.model.From;
import com.marcicomentariosfacebook.model.Reaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MapperCommentsReactions {

    public Flux<Comment> mapComments(FbCommentsReactionsResp response) {
        if (response.getComments() == null || response.getComments().getData() == null) {
            return Flux.empty();
        }

        List<Comment> orderedComments = new ArrayList<>();

        for (FbCommentsReactionsResp.Comment fbComment : response.getComments().getData()) {
            flattenCommentsRecursively(fbComment, response.getId(), null, orderedComments);
        }

        return Flux.fromIterable(orderedComments);
    }

    private void flattenCommentsRecursively(FbCommentsReactionsResp.Comment fbComment, String postId, String parentId, List<Comment> collector) {
        if (fbComment == null || fbComment.getId() == null) {
            return;
        }

        Comment.CommentBuilder builder = Comment.builder()
                .id(fbComment.getId())
                .message(fbComment.getMessage())
                .created_time(fbComment.getCreated_time())
                .parentId(parentId)
                .postId(postId);

        if (fbComment.getFrom() != null) {
            builder.from_id(fbComment.getFrom().getId());
        } else {
            //log.warn("⚠️ Comentario {} sin autor (from). Se guardará con from_id null", fbComment.getId());
        }

        collector.add(builder.build());

        if (fbComment.getComments() != null && fbComment.getComments().getData() != null) {
            for (FbCommentsReactionsResp.Comment child : fbComment.getComments().getData()) {
                flattenCommentsRecursively(child, postId, fbComment.getId(), collector);
            }
        }
    }

    public Flux<Reaction> mapReactions(FbCommentsReactionsResp response) {
        Flux<Reaction> postReactions = Flux.empty();

        if (response.getReactions() != null && response.getReactions().getData() != null) {
            postReactions = Flux.fromIterable(response.getReactions().getData())
                    .map(reaction -> Reaction.builder()
                            .id(null)
                            .user_id(reaction.getId())
                            .user_name(reaction.getName())
                            .type(reaction.getType())
                            .comment_id(null)
                            .post_id(response.getId())
                            .build());
        }

        if (response.getReactions() != null && response.getReactions().getSummary() != null
                && !"NONE".equals(response.getReactions().getSummary().getViewer_reaction())
                && response.getFrom() != null) {
            Reaction authorReaction = Reaction.builder()
                    .id(null)
                    .user_id(response.getFrom().getId())
                    .user_name(response.getFrom().getName())
                    .type(response.getReactions().getSummary().getViewer_reaction())
                    .comment_id(null)
                    .post_id(response.getId())
                    .build();
            postReactions = Flux.concat(postReactions, Flux.just(authorReaction));
        }

        Flux<Reaction> commentReactions = Flux.empty();
        if (response.getComments() != null && response.getComments().getData() != null) {
            commentReactions = Flux.fromIterable(response.getComments().getData())
                    .flatMap(comment -> {
                        if (comment.getReactions() == null) return Flux.empty();

                        Flux<Reaction> reactions = comment.getReactions().getData() != null
                                ? Flux.fromIterable(comment.getReactions().getData())
                                .map(reaction -> Reaction.builder()
                                        .id(null)
                                        .user_id(reaction.getId())
                                        .user_name(reaction.getName())
                                        .type(reaction.getType())
                                        .comment_id(comment.getId())
                                        .post_id(response.getId())
                                        .build())
                                : Flux.empty();

                        if (comment.getReactions().getSummary() != null
                                && !"NONE".equals(comment.getReactions().getSummary().getViewer_reaction())
                                && comment.getFrom() != null) {
                            Reaction authorReaction = Reaction.builder()
                                    .id(null)
                                    .user_id(comment.getFrom().getId())
                                    .user_name(comment.getFrom().getName())
                                    .type(comment.getReactions().getSummary().getViewer_reaction())
                                    .comment_id(comment.getId())
                                    .post_id(response.getId())
                                    .build();

                            reactions = Flux.concat(reactions, Flux.just(authorReaction));
                        }

                        return reactions;
                    });
        }

        return Flux.merge(postReactions, commentReactions);
    }

    public Flux<From> mapFroms(FbCommentsReactionsResp response) {
        if (response == null) {
            return Flux.empty();
        }

        Flux<From> postAuthor = response.getFrom() != null
                ? Flux.just(From.builder()
                .id(response.getFrom().getId())
                .name(response.getFrom().getName())
                .build())
                : Flux.empty();

        Flux<From> commentUsers = Flux.empty();
        if (response.getComments() != null && response.getComments().getData() != null) {
            commentUsers = Flux.fromIterable(response.getComments().getData())
                    .flatMap(comment -> {
                        Flux<From> users = comment.getFrom() != null
                                ? Flux.just(From.builder()
                                .id(comment.getFrom().getId())
                                .name(comment.getFrom().getName())
                                .build())
                                : Flux.empty();

                        if (comment.getComments() != null && comment.getComments().getData() != null) {
                            Flux<From> replyUsers = Flux.fromIterable(comment.getComments().getData())
                                    .filter(reply -> reply.getFrom() != null)
                                    .map(reply -> From.builder()
                                            .id(reply.getFrom().getId())
                                            .name(reply.getFrom().getName())
                                            .build());
                            users = Flux.concat(users, replyUsers);
                        }

                        if (comment.getReactions() != null && comment.getReactions().getData() != null) {
                            Flux<From> reactionUsers = Flux.fromIterable(comment.getReactions().getData())
                                    .map(reaction -> From.builder()
                                            .id(reaction.getId())
                                            .name(reaction.getName())
                                            .build());
                            users = Flux.concat(users, reactionUsers);
                        }

                        return users;
                    });
        }

        Flux<From> postReactionUsers = Flux.empty();
        if (response.getReactions() != null && response.getReactions().getData() != null) {
            postReactionUsers = Flux.fromIterable(response.getReactions().getData())
                    .map(reaction -> From.builder()
                            .id(reaction.getId())
                            .name(reaction.getName())
                            .build());
        }

        return Flux.merge(postAuthor, commentUsers, postReactionUsers)
                .distinct(From::getId);
    }
}
