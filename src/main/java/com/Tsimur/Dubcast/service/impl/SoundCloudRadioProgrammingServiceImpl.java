package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.PlaylistMapper;
import com.Tsimur.Dubcast.mapper.ScheduleEntryMapper;
import com.Tsimur.Dubcast.model.Playlist;
import com.Tsimur.Dubcast.model.PlaylistTrack;
import com.Tsimur.Dubcast.model.ScheduleEntry;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.repository.PlaylistRepository;
import com.Tsimur.Dubcast.repository.PlaylistTrackRepository;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class SoundCloudRadioProgrammingServiceImpl implements RadioProgrammingService {
    private TrackService trackService;

    private ParserService parserService;

    private final PlaylistTrackRepository playlistTrackRepository;

    private final PlaylistRepository playlistRepository;
    private final PlaylistMapper playlistMapper;

    private final ScheduleEntryService scheduleEntryService;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;


    @Override
    public TrackDto createTrackFromUrl(String soundcloudUrl) {
        TrackDto parsed = parserService.parseTracksByUrl(soundcloudUrl);
        String UrlOfParsedTrack = parsed.getSoundcloudUrl();

        return trackService.create(parsed);
    }

    @Deprecated
    @Override
    public ScheduleEntryDto createTrackFromUrlAndScheduleNow(String soundcloudUrl) {
        TrackDto track = createTrackFromUrl(soundcloudUrl);
        return scheduleEntryService.scheduleNow(track.getId());
    }

    @Deprecated
    @Override
    public ScheduleEntryDto scheduleExistingTrackNow(Long trackId) {
        return scheduleEntryService.scheduleNow(trackId);
    }

    @Override
    @Transactional
    public PlaylistScheduleResponse appendPlaylistToSchedule(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist not found: " + playlistId));

        // --- выбираем корректную точку старта ---
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime lastEnd = scheduleEntryRepository.findMaxEndTime();

        OffsetDateTime startTime;
        if (lastEnd == null || lastEnd.isBefore(now)) {
            // расписание пустое или уже всё в прошлом — начинаем с "сейчас"
            startTime = now;
        } else {
            // есть будущие записи — продолжаем с последнего конца
            startTime = lastEnd;
        }
        // ----------------------------------------

        List<PlaylistTrack> pts =
                playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        List<ScheduleEntryDto> result = new ArrayList<>();

        for (PlaylistTrack pt : pts) {
            Track track = pt.getTrack();
            Integer duration = track.getDurationSeconds();
            if (duration == null || duration <= 0) {
                continue;
            }

            OffsetDateTime endTime = startTime.plusSeconds(duration);

            ScheduleEntry entry = ScheduleEntry.builder()
                    .track(track)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            ScheduleEntry saved = scheduleEntryRepository.save(entry);
            result.add(scheduleEntryMapper.toDto(saved));

            startTime = endTime;
        }

        PlaylistDto playlistDto = playlistMapper.toDto(playlist);

        return PlaylistScheduleResponse.builder()
                .playlist(playlistDto)
                .scheduleEntries(result)
                .build();
    }


}
