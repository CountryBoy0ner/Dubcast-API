package com.Tsimur.Dubcast.analytics.service;

import java.time.Instant;
import lombok.Data;

@Data
class SessionState {
  private Instant lastSeen;
  private String page;
  private Long trackId;
}
