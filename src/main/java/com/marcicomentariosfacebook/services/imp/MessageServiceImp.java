package com.marcicomentariosfacebook.services.imp;

import com.marcicomentariosfacebook.client.LHIA.service.ApiLhiaService;
import com.marcicomentariosfacebook.services.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Service
public class MessageServiceImp implements MessageService {

    private final ApiLhiaService apiLhiaService;

    @Override
    public Mono<String> generateNewMessage(String message_type, String prompt) {
        switch (message_type.toLowerCase()) {
            case "lhia":
                return apiLhiaService.sendMesssageToLhia(prompt);

           /*
            case "plantilla":
                return generatePlantillaMessage(prompt);

            case "mejorada":
                return generateMejoradaMessage(prompt);
            */

            default:
                return Mono.error(new IllegalArgumentException("❌ Tipo de mensaje no soportado: " + message_type));
        }
    }
}