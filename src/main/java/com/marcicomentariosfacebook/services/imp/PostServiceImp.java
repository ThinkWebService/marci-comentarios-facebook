package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.model.Post;
import com.marcicomentariosfacebook.repositories.PostRepository;
import com.marcicomentariosfacebook.services.PostService;
import lombok.AllArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class PostServiceImp implements PostService {

    private final PostRepository postRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Post> save(Post post) {
        return postRepository.existsById(post.getId())
            .flatMap(exists -> {
                if (exists) {
                    return postRepository.save(post);
                } else {
                    return r2dbcEntityTemplate.insert(Post.class).using(post);
                }
            });
    }

    @Override
    public Mono<Post> findById(String id) {
        return postRepository.findById(id);
    }

    @Override
    public Flux<Post> findAll() {
        return postRepository.findAll();
    }

}
