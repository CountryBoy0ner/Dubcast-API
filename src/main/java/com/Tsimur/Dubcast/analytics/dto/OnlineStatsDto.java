package com.Tsimur.Dubcast.analytics.dto;



import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
public class OnlineStatsDto {
    private int totalOnline;
    private Map<Long, Integer> onlinePerTrack;
    private OffsetDateTime generatedAt;
}
