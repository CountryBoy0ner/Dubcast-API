package com.Tsimur.Dubcast.analytics.dto;

import lombok.Data;

@Data
public class AnalyticsHeartbeatMessage {
  private String page; // например "/radio"
  private boolean listening; // true = слушаю, false = перестал
  private Long trackId; // опционально, можно пока не использовать
}
