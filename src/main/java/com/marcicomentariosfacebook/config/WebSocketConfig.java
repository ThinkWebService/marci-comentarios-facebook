package com.marcicomentariosfacebook.config;

import com.marcicomentariosfacebook.websocket.CommentWebSocketHandler;
import com.marcicomentariosfacebook.websocket.ResumenWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import java.util.Map;

@Configuration
public class WebSocketConfig {

    @Bean
    public SimpleUrlHandlerMapping webSocketMapping(CommentWebSocketHandler commentHandler, ResumenWebSocketHandler resumenHandler) {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(-1);
        mapping.setUrlMap(Map.of(
                "/ws/facebook", commentHandler,   // Para comentarios
                "/ws/resumen", resumenHandler  // Para resumenes
        ));
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}