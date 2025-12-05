package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.config.RadioTimeConfig;
import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
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
import com.Tsimur.Dubcast.radio.events.ScheduleUpdatedEvent;
import com.Tsimur.Dubcast.repository.PlaylistRepository;
import com.Tsimur.Dubcast.repository.PlaylistTrackRepository;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.*;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class SoundCloudRadioProgrammingServiceImpl implements RadioProgrammingService {


    private final TrackRepository trackRepository;
    private final PlaylistTrackRepository playlistTrackRepository;
    private final PlaylistRepository playlistRepository;
    private final PlaylistMapper playlistMapper;

    private final ScheduleEntryService scheduleEntryService;
    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;

    private final ApplicationEventPublisher eventPublisher;
    private final RadioTimeConfig radioTimeConfig;


    @Override
    @Transactional
    public PlaylistScheduleResponse appendPlaylistToSchedule(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist not found: " + playlistId));

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime maxEndTime = scheduleEntryRepository.findMaxEndTime();

        OffsetDateTime startTime =
                (maxEndTime == null || maxEndTime.isBefore(now))
                        ? now
                        : maxEndTime;

        List<PlaylistTrack> pts =
                playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        List<ScheduleEntryDto> result = new ArrayList<>();
        OffsetDateTime firstStartTime = null;

        for (PlaylistTrack pt : pts) {
            Track track = pt.getTrack();
            Integer duration = track.getDurationSeconds();
            if (duration == null || duration <= 0) {
                continue;
            }

            OffsetDateTime endTime = startTime.plusSeconds(duration);

            ScheduleEntry entry = ScheduleEntry.builder()
                    .track(track)
                    .playlist(playlist)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            if (firstStartTime == null) {
                firstStartTime = startTime;
            }

            ScheduleEntry saved = scheduleEntryRepository.save(entry);
            result.add(scheduleEntryMapper.toDto(saved));

            startTime = endTime;
        }

        if (firstStartTime != null) {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(firstStartTime));
        }

        PlaylistDto playlistDto = playlistMapper.toDto(playlist);

        return PlaylistScheduleResponse.builder()
                .playlist(playlistDto)
                .scheduleEntries(result)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleEntryDto> getCurrentSlot(OffsetDateTime now) {
        return scheduleEntryService.getCurrent(now);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleEntryDto> getNextSlot(OffsetDateTime now) {
        return scheduleEntryService.getNext(now);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleEntryDto> getPreviousSlot(OffsetDateTime now) {
        return scheduleEntryService.getPrevious(now);
    }


    @Override
    @Transactional
    public ScheduleEntryDto appendTrackToSchedule(Long trackId) {


        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new NotFoundException("Track not found: " + trackId));

        Integer duration = track.getDurationSeconds();
        if (duration == null || duration <= 0) {
            throw new IllegalStateException("Track " + trackId + " has no valid duration");
        }


        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime maxEndTime = scheduleEntryRepository.findMaxEndTime();

        OffsetDateTime startTime =
                (maxEndTime == null || maxEndTime.isBefore(now))
                        ? now
                        : maxEndTime;

        OffsetDateTime endTime = startTime.plusSeconds(duration);

        ScheduleEntry entry = ScheduleEntry.builder()
                .track(track)
                .playlist(null)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        ScheduleEntry saved = scheduleEntryRepository.save(entry);
        ScheduleEntryDto dto = scheduleEntryMapper.toDto(saved);
        eventPublisher.publishEvent(new ScheduleUpdatedEvent(startTime));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminScheduleSlotDto> getDaySchedule(LocalDate date, Pageable pageable) {
        Page<ScheduleEntryDto> page = scheduleEntryService.getDayPage(date, pageable);

        Set<Long> playlistIds = page.stream()
                .map(ScheduleEntryDto::getPlaylistId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, String> playlistNames = playlistRepository.findAllById(playlistIds).stream()
                .collect(Collectors.toMap(Playlist::getId, Playlist::getName));

        var zone = radioTimeConfig.getRadioZoneId();

        return page.map(se -> AdminScheduleSlotDto.builder()
                .id(se.getId())
                .trackTitle(se.getTrack().getTitle())
                .trackArtworkUrl(se.getTrack().getArtworkUrl())
                .trackScUrl(se.getTrack().getSoundcloudUrl())
                .playlistId(se.getPlaylistId())
                .playlistName(
                        se.getPlaylistId() != null
                                ? playlistNames.get(se.getPlaylistId())
                                : null
                )
                .startTime(OffsetDateTime.ofInstant(se.getStartTime(), zone))
                .endTime(OffsetDateTime.ofInstant(se.getEndTime(), zone))
                .build()
        );
    }


}
