package com.marcicomentariosfacebook.utils.fechas;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CustomDateDeserializer extends StdDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final ZoneId ECUADOR_ZONE = ZoneId.of("America/Guayaquil");

    public CustomDateDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText();
        // Parsear con formatter a ZonedDateTime y luego convertir a la zona Ecuador
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, FORMATTER);
        ZonedDateTime ecuadorTime = zonedDateTime.withZoneSameInstant(ECUADOR_ZONE);
        return ecuadorTime.toLocalDateTime();
    }
}

