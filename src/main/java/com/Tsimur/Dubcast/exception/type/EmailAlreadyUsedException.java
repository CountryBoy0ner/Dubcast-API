package com.Tsimur.Dubcast.exception.type;

public class EmailAlreadyUsedException extends RuntimeException {
  public EmailAlreadyUsedException(String message) {
    super(message);
  }
}
