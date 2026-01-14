package com.Tsimur.Dubcast.analytics.dto;

import lombok.Data;

@Data
public class AnalyticsHeartbeatMessage {
  private String page;
  private boolean listening;
  private Long trackId;
}
