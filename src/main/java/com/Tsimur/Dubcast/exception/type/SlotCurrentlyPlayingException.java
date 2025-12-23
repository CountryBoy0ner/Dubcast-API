package com.Tsimur.Dubcast.exception.type;

public class SlotCurrentlyPlayingException extends RuntimeException {
  public SlotCurrentlyPlayingException(Long slotId) {
    super("Cannot delete currently playing slot: " + slotId);
  }
}
