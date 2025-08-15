package com.marcicomentariosfacebook.init;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.services.*;
import com.marcicomentariosfacebook.utils.maper.responses.MapperCommentsReactions;
import com.marcicomentariosfacebook.utils.maper.responses.MapperPage;
import com.marcicomentariosfacebook.utils.maper.responses.MapperPosts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import org.springframework.r2dbc.core.DatabaseClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer {

    private final DatabaseClient databaseClient;
    private final APIGraphService apiGraphService;

    private final MapperPage pageMapper;
    private final PageService pageService;

    private final MapperPosts mapperPosts;
    private final PostService postService;

    private final MapperCommentsReactions mapperCommentsReactions;
    private final FromService fromService;
    private final CommentService commentService;
    private final ReactionService reactionService;

    private final PlantillaTypeService plantillaTypeService;

    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            initSchema();
            initData();
            log.info("🟢 Inicialización completa, app lista para recibir solicitudes");
        } catch (Exception e) {
            log.error("🔴 Inicialización con errores:", e);
            System.exit(1);
        }
    }

    private void initSchema() {
        try {
            ClassPathResource resource = new ClassPathResource("schema.sql");
            String schema = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            databaseClient.sql(schema)
                    .then()
                    .doOnSuccess(v -> log.info("✅ Esquema DB listo"))
                    .block();
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo schema.sql", e);
        }
    }

    private void initData() {
        apiGraphService.getPageInfo()
                .flatMap(pageMapper::pageResponseToPage)
                .flatMap(pageService::save)
                .flatMap(savedPage -> apiGraphService.getPosts()
                        .flatMapMany(mapperPosts::postsFacebookToPost)
                        .concatMap(postService::save)
                        .concatMap(savedPost -> apiGraphService.getCommentsReactionsByPostId(savedPost.getId())
                                .flatMapMany(response -> mapperCommentsReactions.mapFroms(response)
                                        .flatMap(fromService::save)
                                        .thenMany(mapperCommentsReactions.mapComments(response)
                                                .collectList()
                                                .flatMapMany(commentService::saveAll))
                                        .thenMany(mapperCommentsReactions.mapReactions(response)
                                                .flatMap(reactionService::save))))
                        .then())
                .doOnSuccess(v -> log.info("✅ Datos de Facebook cargados y guardados (Posts, Comments, Reactions)"))
                .block();
    }
}