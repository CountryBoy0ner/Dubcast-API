package com.Tsimur.Dubcast.exception.type;

public class BadRequestException extends RuntimeException {

  public BadRequestException(String message) {
    super(message);
  }

  public BadRequestException(String message, Throwable cause) {
    super(message, cause);
  }

  public static BadRequestException of(String entity, String field, String reason) {
    String msg = String.format("%s: bad request for %s - %s", entity, field, reason);
    return new BadRequestException(msg);
  }
}
