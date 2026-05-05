package com.hotel.management.jpa.shared;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonColumnCodec {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonColumnCodec() {
    }

    public static <T> String write(T value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to write JSON column", ex);
        }
    }

    public static <T> T read(String value, TypeReference<T> typeReference, T fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return OBJECT_MAPPER.readValue(value, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to read JSON column", ex);
        }
    }
}
