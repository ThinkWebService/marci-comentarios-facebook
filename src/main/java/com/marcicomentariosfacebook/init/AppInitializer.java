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
import org.springframework.stereotype.Component;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;

import java.nio.file.Files;
import java.nio.file.Paths;

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


    @EventListener(ApplicationReadyEvent.class)
    public void initialize() {
        try {
            initSchema();
            initData();
            log.info("🟢 Inicialización completa, app lista para recibir solicitudes");
        } catch (Exception e) {
            log.info("🔴 Inicialización con errores: {}", e);
            System.exit(1);
        }
    }

    private void initSchema() throws Exception {
        String schema = Files.readString(Paths.get("src/main/resources/schema.sql"));

        Flux.fromArray(schema.split(";"))
                .map(String::trim)
                .filter(sql -> !sql.isEmpty())
                .flatMap(sql -> databaseClient.sql(sql).then())
                .then()
                .doOnSuccess(v -> log.info("✅ Esquema DB listo"))
                .block();  // Bloqueamos para esperar
    }

    private void initData() {
        apiGraphService.getPageInfo()
                .flatMap(pageMapper::pageResponseToPage)
                .flatMap(pageService::save)
                .flatMap(savedPage -> apiGraphService.getPosts()
                        .flatMapMany(mapperPosts::postsFacebookToPost)
                        .concatMap(postService::save) // GUARDA POSTS UNO A UNO
                        .concatMap(savedPost ->
                                apiGraphService.getCommentsReactionsByPostId(savedPost.getId())
                                        .flatMapMany(response -> {
                                            //log.info("RESPONSE -> COMMENTS | REACTIONS: {}", response);

                                            return mapperCommentsReactions.mapFroms(response)
                                                    .flatMap(fromService::save)
                                                    .thenMany(mapperCommentsReactions.mapComments(response)
                                                            .collectList()
                                                            .flatMapMany(commentService::saveAll)) // Inserta todos los comentarios ya ordenados
                                                    .thenMany(mapperCommentsReactions.mapReactions(response)
                                                            .flatMap(reactionService::save));
                                        })
                        )
                        .then()
                )
                .doOnSuccess(v -> log.info("✅ Datos de Facebook cargados y guardados (Posts, Comments, Reactions)"))
                .block(); // Espera a que todo se guarde
    }
}
