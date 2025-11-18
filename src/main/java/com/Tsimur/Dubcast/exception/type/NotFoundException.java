package com.Tsimur.Dubcast.exception.type;


public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String entity, String field, Object value) {
        return new NotFoundException(entity + " with " + field + " = " + value + " not found");
    }
}