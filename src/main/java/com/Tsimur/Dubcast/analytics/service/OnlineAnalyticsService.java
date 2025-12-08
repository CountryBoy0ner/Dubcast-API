package com.Tsimur.Dubcast.analytics.service;


import com.Tsimur.Dubcast.analytics.dto.AnalyticsHeartbeatMessage;
import com.Tsimur.Dubcast.analytics.dto.OnlineStatsDto;

public interface OnlineAnalyticsService {
    void handleHeartbeat(String sessionId, AnalyticsHeartbeatMessage msg);

    OnlineStatsDto getCurrentStats();
}
