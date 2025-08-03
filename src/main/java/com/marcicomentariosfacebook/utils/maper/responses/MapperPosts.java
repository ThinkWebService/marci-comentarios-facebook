package com.marcicomentariosfacebook.utils.maper.responses;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbPostsResp;
import com.marcicomentariosfacebook.client.FACEBOOK.models.FacebookPost;
import com.marcicomentariosfacebook.model.Post;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

@Component
public class MapperPosts {

    public Flux<Post> postsFacebookToPost(FbPostsResp fbPostsResp) {
        List<FacebookPost> postList = fbPostsResp.getPosts().getData();
        return Flux.fromIterable(postList)
                .map(post -> Post.builder()
                        .id(post.getId())
                        .message(post.getMessage())
                        .full_picture(post.getFull_picture())
                        .permalink_url(post.getPermalink_url())
                        .created_time(post.getCreated_time())
                        .updated_time(post.getUpdated_time())
                        .story(post.getStory())
                        .status_type(post.getStatus_type())
                        .published(post.isPublished())
                        .page_id(fbPostsResp.getId())
                        .build()
                );
    }
}