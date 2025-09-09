package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.services.EventoHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventoHandlerFactory {

    private final CommentEventoHandler commentHandler;
    private final ReactionEventoHandler reactionHandler;
    private final PostEventoHandler postHandler;

    public EventoHandler getHandler(String item) {
        return switch (item) {
            case "comment" -> commentHandler;
            // case "reaction" -> reactionHandler; // no se maneja por ahora
            case "post","photo", "status", "video" -> postHandler;
            default -> null;
        };
    }
}