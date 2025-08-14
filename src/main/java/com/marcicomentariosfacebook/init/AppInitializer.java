package com.marcicomentariosfacebook.init;

import com.marcicomentariosfacebook.client.FACEBOOK.services.APIGraphService;
import com.marcicomentariosfacebook.model.Plantilla;
import com.marcicomentariosfacebook.model.PlantillaType;
import com.marcicomentariosfacebook.services.*;
import com.marcicomentariosfacebook.utils.maper.responses.MapperCommentsReactions;
import com.marcicomentariosfacebook.utils.maper.responses.MapperPage;
import com.marcicomentariosfacebook.utils.maper.responses.MapperPosts;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer {

        private final DatabaseClient databaseClient;
        private final APIGraphService apiGraphService;
        private final Environment env;
        private final MapperPage pageMapper;
        private final PageService pageService;

        private final MapperPosts mapperPosts;
        private final PostService postService;

        private final MapperCommentsReactions mapperCommentsReactions;
        private final FromService fromService;
        private final CommentService commentService;
        private final ReactionService reactionService;

        private final PlantillaService plantillaService;

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
                initPlantillas().then(
                                apiGraphService.getPageInfo()
                                                .flatMap(pageMapper::pageResponseToPage)
                                                .flatMap(pageService::save)
                                                .flatMap(savedPage -> apiGraphService.getPosts()
                                                                .flatMapMany(mapperPosts::postsFacebookToPost)
                                                                .concatMap(postService::save)
                                                                .concatMap(savedPost -> apiGraphService
                                                                                .getCommentsReactionsByPostId(
                                                                                                savedPost.getId())
                                                                                .flatMapMany(response -> mapperCommentsReactions
                                                                                                .mapFroms(response)
                                                                                                .flatMap(fromService::save)
                                                                                                .thenMany(mapperCommentsReactions
                                                                                                                .mapComments(response)
                                                                                                                .collectList()
                                                                                                                .flatMapMany(commentService::saveAll))
                                                                                                .thenMany(mapperCommentsReactions
                                                                                                                .mapReactions(response)
                                                                                                                .flatMap(reactionService::save))))
                                                                .then()))
                                .doOnSuccess(v -> log.info(
                                                "✅ Datos de Facebook cargados y guardados (Posts, Comments, Reactions)"))
                                .block();
        }

        private Mono<Void> initPlantillas() {
                List<Plantilla> plantillas = List.of(
                                Plantilla.builder()
                                                .id("1")
                                                .name("Disponibilidad del producto")
                                                .descripcion("¡Hola! Sí, el producto aún está disponible. ¿Quieres que te ayude con algún detalle adicional?")
                                                .enlace(null)
                                                .plantilla_type(PlantillaType.DISPONIBILIDAD)
                                                .build(),

                                Plantilla.builder()
                                                .id("2")
                                                .name("Horario de atención")
                                                .descripcion("Nuestro horario de atención es de lunes a viernes de 9:00 a 18:00. Estamos para ayudarte en ese horario.")
                                                .enlace(null)
                                                .plantilla_type(PlantillaType.INFORMACION)
                                                .build(),

                                Plantilla.builder()
                                                .id("3")
                                                .name("Precio del producto")
                                                .descripcion("El precio actual es el que aparece en nuestra tienda online. Si quieres, te puedo enviar el enlace.")
                                                .enlace("https://tusitio.com/productos")
                                                .plantilla_type(PlantillaType.PRECIO)
                                                .build(),

                                Plantilla.builder()
                                                .id("4")
                                                .name("Envío y entregas")
                                                .descripcion("Ofrecemos envío a domicilio con un costo adicional según la zona. También puedes recoger en tienda sin costo.")
                                                .enlace("https://tusitio.com/envios")
                                                .plantilla_type(PlantillaType.ENVIOS)
                                                .build(),

                                Plantilla.builder()
                                                .id("5")
                                                .name("Métodos de pago")
                                                .descripcion("Aceptamos pagos con tarjeta de crédito, débito y transferencias bancarias. ¿Quieres más información?")
                                                .enlace(null)
                                                .plantilla_type(PlantillaType.PAGO)
                                                .build(),

                                Plantilla.builder()
                                                .id("6")
                                                .name("Garantía del producto")
                                                .descripcion("Todos nuestros productos cuentan con garantía de 6 meses. Si tienes algún problema, contáctanos.")
                                                .enlace(null)
                                                .plantilla_type(PlantillaType.GARANTIA)
                                                .build(),

                                Plantilla.builder()
                                                .id("7")
                                                .name("Política de devoluciones")
                                                .descripcion("Puedes devolver productos dentro de los 15 días siguientes a la compra, siempre que estén en buen estado.")
                                                .enlace("https://tusitio.com/devoluciones")
                                                .plantilla_type(PlantillaType.DEVOLUCIONES)
                                                .build(),

                                Plantilla.builder()
                                                .id("8")
                                                .name("Promociones vigentes")
                                                .descripcion("Actualmente tenemos promociones especiales en algunos productos. Visita nuestro sitio para más detalles.")
                                                .enlace("https://tusitio.com/promociones")
                                                .plantilla_type(PlantillaType.PROMOCIONES)
                                                .build(),

                                Plantilla.builder()
                                                .id("9")
                                                .name("Consulta técnica")
                                                .descripcion("Para dudas técnicas, puedes revisar nuestro FAQ o contactarnos directamente para ayudarte mejor.")
                                                .enlace("https://tusitio.com/faq")
                                                .plantilla_type(PlantillaType.TECNICA)
                                                .build(),

                                Plantilla.builder()
                                                .id("10")
                                                .name("Tiempo de entrega")
                                                .descripcion("El tiempo estimado de entrega es de 3 a 5 días hábiles dependiendo de tu ubicación.")
                                                .enlace(null)
                                                .plantilla_type(PlantillaType.ENTREGA)
                                                .build(),

                                Plantilla.builder()
                                                .id("11")
                                                .name("Contacto para soporte")
                                                .descripcion("Si necesitas ayuda personalizada, puedes contactarnos vía chat o teléfono.")
                                                .enlace("https://tusitio.com/contacto")
                                                .plantilla_type(PlantillaType.SOPORTE)
                                                .build(),

                                Plantilla.builder()
                                                .id("12")
                                                .name("Gracias por tu comentario")
                                                .descripcion("Gracias por escribirnos, estamos atentos para ayudarte con cualquier otra consulta que tengas.")
                                                .enlace(null)
                                                .plantilla_type(PlantillaType.AGRADECIMIENTO)
                                                .build());

                return plantillaService.saveAll(plantillas).then();
        }

}
