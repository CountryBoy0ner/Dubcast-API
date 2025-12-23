package com.Tsimur.Dubcast.analytics.dto;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnlineStatsDto {
  private int totalOnline;
  @Deprecated private Map<Long, Integer> onlinePerTrack;
  private OffsetDateTime generatedAt;
}
