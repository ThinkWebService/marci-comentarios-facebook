package com.marcicomentariosfacebook.init;

import com.marcicomentariosfacebook.services.VistaResumenService;
import com.marcicomentariosfacebook.websocket.ResumenWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostgresNotificationListener implements Runnable {

    private final DataSource dataSource;
    private final VistaResumenService vistaResumenService;
    private final ResumenWebSocketHandler resumenWebSocketHandler;

    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        Thread thread = new Thread(this, "PostgresNotificationListener");
        thread.setDaemon(true);
        thread.start();
    }

    @PreDestroy
    public void stop() {
        running = false;
    }

    @Override
    public void run() {
        try (Connection conn = dataSource.getConnection()) {
            PGConnection pgconn = conn.unwrap(PGConnection.class);

            try (Statement stmt = conn.createStatement()) {
                stmt.execute("LISTEN post_update");
                //log.info("🔔 LISTEN en canal post_update activo");
            }

            while (running) {
                PGNotification[] notifications = pgconn.getNotifications(5000);

                if (notifications != null) {
                    for (PGNotification notification : notifications) {
                        String postId = notification.getParameter();
                        //log.info("📣 Notificación recibida para postId: {}", postId);

                        vistaResumenService.findById(postId)
                                .flatMap(resumen -> resumenWebSocketHandler.emitirResumen(List.of(resumen)))
                                .subscribe();
                    }
                }
                Thread.sleep(100);
            }
        } catch (Exception e) {
            log.error("Error en listener de PostgreSQL: ", e);
        }
    }
}