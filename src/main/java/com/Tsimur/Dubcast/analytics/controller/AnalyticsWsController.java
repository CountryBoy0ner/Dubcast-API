package com.Tsimur.Dubcast.analytics.controller;

import com.Tsimur.Dubcast.analytics.dto.AnalyticsHeartbeatMessage;
import com.Tsimur.Dubcast.analytics.dto.OnlineStatsDto;
import com.Tsimur.Dubcast.analytics.service.OnlineAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class AnalyticsWsController {

  private final OnlineAnalyticsService analyticsService;
  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/analytics.heartbeat")
  public void onHeartbeat(
      AnalyticsHeartbeatMessage msg, @Header("simpSessionId") String sessionId) {

    analyticsService.handleHeartbeat(sessionId, msg);

    OnlineStatsDto stats = analyticsService.getCurrentStats();

    messagingTemplate.convertAndSend("/topic/analytics/online", stats);
  }
}
