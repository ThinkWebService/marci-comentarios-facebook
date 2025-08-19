package com.marcicomentariosfacebook.services.factory;

import com.marcicomentariosfacebook.services.EventoHandler;
import com.marcicomentariosfacebook.services.factory.CommentEventoHandler;
import com.marcicomentariosfacebook.services.factory.PostEventoHandler;
import com.marcicomentariosfacebook.services.factory.ReactionEventoHandler;
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
            case "reaction" -> reactionHandler;
            case "photo", "status", "video" -> postHandler;
            default -> null;
        };
    }
}