package com.takypok.report.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class MicrosecondEpochLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final ZoneId ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (p.currentToken() == JsonToken.VALUE_NULL) return null;
        long micros = p.getLongValue();
        long millis = micros / 1_000;
        return Instant.ofEpochMilli(millis).atZone(ZONE).toLocalDate();
    }
}
