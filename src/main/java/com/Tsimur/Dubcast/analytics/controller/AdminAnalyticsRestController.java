package com.Tsimur.Dubcast.analytics.controller;


import com.Tsimur.Dubcast.analytics.service.OnlineAnalyticsService;
import com.Tsimur.Dubcast.analytics.dto.OnlineStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
public class AdminAnalyticsRestController {

    private final OnlineAnalyticsService analyticsService;

    @GetMapping("/online")
    public ResponseEntity<OnlineStatsDto> getOnlineStats() {
        return ResponseEntity.ok(analyticsService.getCurrentStats());
    }
}
