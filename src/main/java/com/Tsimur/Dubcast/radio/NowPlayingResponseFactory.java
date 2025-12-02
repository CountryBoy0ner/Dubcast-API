
package com.Tsimur.Dubcast.radio;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.NowPlayingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NowPlayingResponseFactory {

    public NowPlayingResponse fromScheduleEntry(ScheduleEntryDto entry) {
        // На всякий случай защитимся от null
        // todo за
        

        TrackDto track = entry.getTrack();

        Integer duration = track.getDurationSeconds();
        if (duration == null || duration < 0) {
            duration = 0;
        }

        return NowPlayingResponse.builder()
                .playing(true)
                .title(track.getTitle())
                .artworkUrl(track.getArtworkUrl())

                .startedAt(entry.getStartTime())
                .durationSeconds(duration)

                .trackUrl(track.getSoundcloudUrl())
                .build();
    }
}
