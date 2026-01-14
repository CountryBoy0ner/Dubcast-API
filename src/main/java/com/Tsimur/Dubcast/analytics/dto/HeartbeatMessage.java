package com.Tsimur.Dubcast.analytics.dto;

import lombok.Data;

@Data
public class HeartbeatMessage {
  private String page;
  private Long trackId;
}
