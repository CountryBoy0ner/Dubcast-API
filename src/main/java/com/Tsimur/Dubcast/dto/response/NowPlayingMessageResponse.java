package com.Tsimur.Dubcast.dto.response;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NowPlayingMessageResponse {

    private boolean playing;        // есть ли сейчас трек
    private String title;
    private String artworkUrl;
    private String embedCode;

    // ⬇⬇⬇ ДОБАВИЛИ:
    private Integer durationSeconds;
    private Instant startedAt;

    // фабрика из TrackDto (если где-то ещё используется — можно оставить)
    public static NowPlayingMessageResponse from(TrackDto trackDto) {
        return NowPlayingMessageResponse.builder()
                .playing(true)
                .title(trackDto.getTitle())
                .artworkUrl(trackDto.getArtworkUrl())
                .embedCode(trackDto.getEmbedCode())
                .durationSeconds(trackDto.getDurationSeconds())
                .build();
    }

    // новая фабрика именно из ScheduleEntryDto (для WS и REST /now)
    public static NowPlayingMessageResponse from(ScheduleEntryDto entry) {
        if (entry == null) {
            return nothing();
        }

        TrackDto track = entry.getTrack();

        return NowPlayingMessageResponse.builder()
                .playing(true)
                .title(track.getTitle())
                .artworkUrl(track.getArtworkUrl())
                .embedCode(track.getEmbedCode())
                .durationSeconds(track.getDurationSeconds())
                .startedAt(entry.getStartTime())  // у тебя startTime = Instant
                .build();
    }

    public static NowPlayingMessageResponse nothing() {
        return NowPlayingMessageResponse.builder()
                .playing(false)
                .build();
    }
}
