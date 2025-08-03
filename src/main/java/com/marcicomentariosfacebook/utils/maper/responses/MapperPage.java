package com.marcicomentariosfacebook.utils.maper.responses;

import com.marcicomentariosfacebook.client.FACEBOOK.DTOS.FbPageResp;
import com.marcicomentariosfacebook.model.Page;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MapperPage {

    public Mono<Page> pageResponseToPage(FbPageResp facebookPageResponse) {
        return Mono.fromCallable(() -> Page.builder()
                .id(facebookPageResponse.getId())
                .name(facebookPageResponse.getName())
                .username(facebookPageResponse.getUsername())
                .category(facebookPageResponse.getCategory())
                .description(facebookPageResponse.getAbout())
                .link(facebookPageResponse.getLink())
                .fan_count(facebookPageResponse.getFan_count())
                .followers_count(facebookPageResponse.getFollowers_count())
                .cover_url(
                        facebookPageResponse.getCover() != null
                                ? facebookPageResponse.getCover().getSource()
                                : null
                )
                .profile_url(
                        facebookPageResponse.getPicture() != null
                                ? facebookPageResponse.getPicture().getData().getUrl()
                                : null
                )
                .verified(facebookPageResponse.isVerified())
                .build()
        );
    }
}