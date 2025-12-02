
package com.Tsimur.Dubcast.radio;

import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.NowPlayingResponse;
import com.Tsimur.Dubcast.service.PlaylistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class NowPlayingResponseFactory {

    private final PlaylistService playlistService;

    public NowPlayingResponse fromScheduleEntry(ScheduleEntryDto entry) {
        TrackDto track = entry.getTrack();

        Integer duration = track.getDurationSeconds();
        if (duration == null || duration < 0) {
            duration = 0;
        }

        String playlistTitle = null;
        Long playlistId = entry.getPlaylistId();
        if (playlistId != null) {
            try {
                PlaylistDto playlist = playlistService.getById(playlistId);
                playlistTitle = playlist.getTitle();
            } catch (RuntimeException e) {
                //todo
                log.warn("[NowPlayingResponseFactory] Playlist {} not found", playlistId, e);
            }
        }

        return NowPlayingResponse.builder()
                .playing(true)
                .title(track.getTitle())
                .artworkUrl(track.getArtworkUrl())
                .startedAt(entry.getStartTime())
                .durationSeconds(duration)
                .trackUrl(track.getSoundcloudUrl())
                .playlistTitle(playlistTitle)
                .build();
    }
}