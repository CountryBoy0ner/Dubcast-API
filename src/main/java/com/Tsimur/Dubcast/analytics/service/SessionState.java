package com.Tsimur.Dubcast.analytics.service;


import lombok.Data;

import java.time.Instant;

@Data
class SessionState {
    private Instant lastSeen;
    private String page;
    private Long trackId;
}
