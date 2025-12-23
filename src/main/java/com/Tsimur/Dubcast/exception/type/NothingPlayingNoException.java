package com.Tsimur.Dubcast.exception.type;

public class NothingPlayingNoException extends RuntimeException {
  public NothingPlayingNoException() {
    super("Nothing playing");
  }
}
