package com.Tsimur.Dubcast.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class NowPlayingTrackResponse {

    private String title;
    private String artworkUrl;
    private String embedCode;
    private OffsetDateTime startedAt;
    private Integer durationSeconds;

    public static NowPlayingTrackResponse empty() {
        return NowPlayingTrackResponse.builder().build();
    }
}
