package com.Tsimur.Dubcast.analytics.service;

import com.Tsimur.Dubcast.analytics.dto.AnalyticsHeartbeatMessage;
import com.Tsimur.Dubcast.analytics.dto.OnlineStatsDto;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class InMemoryOnlineAnalyticsService implements OnlineAnalyticsService {

  private static final Duration TTL = Duration.ofSeconds(15);

  private final Map<String, ListenerState> sessions = new ConcurrentHashMap<>();

  @Override
  public void handleHeartbeat(String sessionId, AnalyticsHeartbeatMessage msg) {
    OffsetDateTime now = OffsetDateTime.now();
    sessions.compute(
        sessionId,
        (id, old) -> {
          if (!msg.isListening()) {
            return null;
          }
          ListenerState state = (old != null) ? old : new ListenerState();
          state.setLastSeen(now);
          state.setListening(true);
          state.setPage(msg.getPage());
          state.setTrackId(msg.getTrackId());
          return state;
        });
  }

  @Override
  public OnlineStatsDto getCurrentStats() {
    OffsetDateTime now = OffsetDateTime.now();
    sessions.entrySet().removeIf(e -> e.getValue().getLastSeen().isBefore(now.minus(TTL)));
    int total = 0;
    for (ListenerState state : sessions.values()) {
      if (!state.isListening()) {
        continue;
      }
      total++;
      Long trackId = state.getTrackId();
    }
    return OnlineStatsDto.builder().totalOnline(total).generatedAt(now).build();
  }

  @Data
  private static class ListenerState {
    private boolean listening;
    private OffsetDateTime lastSeen;
    private String page;
    private Long trackId;
  }
}
