package com.Tsimur.Dubcast;

public record NowPlayingDto(
        String trackId, String title, String streamUrl,
        long startedAt, long endsAt, long positionMs
) {}