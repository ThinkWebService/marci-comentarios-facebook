package com.marcicomentariosfacebook.utils.fechas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class EpochSecondsDateDeserializer extends StdDeserializer<LocalDateTime> {

    private static final ZoneId ECUADOR_ZONE = ZoneId.of("America/Guayaquil");

    public EpochSecondsDateDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        long epochSeconds = p.getLongValue(); // espera número entero
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ECUADOR_ZONE);
    }
}
