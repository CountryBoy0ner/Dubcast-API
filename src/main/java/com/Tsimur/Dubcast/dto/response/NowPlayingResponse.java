package com.Tsimur.Dubcast.dto.response;

import lombok.*;
import java.time.Instant;

@Builder
@Data
public class NowPlayingResponse {
    private boolean playing;
    private String title;
    private String artworkUrl;
    private Instant startedAt;
    private int durationSeconds;
    private String trackUrl;
}
