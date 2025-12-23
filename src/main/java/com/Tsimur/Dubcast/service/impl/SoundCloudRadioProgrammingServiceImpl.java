package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.config.RadioTimeConfig;
import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.PlaylistMapper;
import com.Tsimur.Dubcast.model.Playlist;
import com.Tsimur.Dubcast.radio.autofill.AutoFillStrategy;
import com.Tsimur.Dubcast.repository.PlaylistRepository;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class SoundCloudRadioProgrammingServiceImpl implements RadioProgrammingService {

  private final PlaylistRepository playlistRepository;
  private final PlaylistMapper playlistMapper;

  private final ScheduleEntryService scheduleEntryService;

  private final RadioTimeConfig radioTimeConfig;
  private final AutoFillStrategy autoFillStrategy;

  @Override
  @Transactional
  public PlaylistScheduleResponse appendPlaylistToSchedule(Long playlistId) {
    Playlist playlist =
        playlistRepository
            .findById(playlistId)
            .orElseThrow(() -> new NotFoundException("Playlist not found: " + playlistId));

    List<ScheduleEntryDto> entries = scheduleEntryService.appendPlaylistToTail(playlistId);

    PlaylistDto playlistDto = playlistMapper.toDto(playlist);

    return PlaylistScheduleResponse.builder()
        .playlist(playlistDto)
        .scheduleEntries(entries)
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
    return scheduleEntryService.appendTrackToTail(trackId);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<AdminScheduleSlotDto> getDaySchedule(LocalDate date, Pageable pageable) {
    Page<ScheduleEntryDto> page = scheduleEntryService.getDayPage(date, pageable);

    // заранее вытаскиваем имена плейлистов, чтобы не делать N+1
    Set<Long> playlistIds =
        page.stream()
            .map(ScheduleEntryDto::getPlaylistId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

    Map<Long, String> playlistNames =
        playlistRepository.findAllById(playlistIds).stream()
            .collect(Collectors.toMap(Playlist::getId, Playlist::getName));

    return page.map(se -> toAdminSlotDto(se, playlistNames));
  }

  @Override
  @Transactional
  public void deleteSlotAndRebuildDay(Long slotId) {
    scheduleEntryService.deleteSlotAndRebuildDay(slotId);
  }

  @Override
  @Transactional
  public AdminScheduleSlotDto insertTrackIntoDay(LocalDate date, Long trackId, int position) {
    ScheduleEntryDto se = scheduleEntryService.insertTrackIntoDay(date, trackId, position);
    return toAdminSlotDto(se, null);
  }

  @Override
  @Transactional
  public AdminScheduleSlotDto changeTrackInSlot(Long slotId, Long newTrackId) {
    ScheduleEntryDto se = scheduleEntryService.changeTrackInSlot(slotId, newTrackId);
    return toAdminSlotDto(se, null);
  }

  @Override
  @Transactional
  public void reorderDay(LocalDate date, List<Long> orderedIds) {
    scheduleEntryService.reorderDay(date, orderedIds);
  }

  @Override
  @Transactional
  public boolean ensureAutofillIfNeeded(OffsetDateTime now) {
    if (scheduleEntryService.getCurrent(now).isPresent()) {
      return false;
    }

    if (scheduleEntryService.getNext(now).isPresent()) {
      return false;
    }

    var trackIdOpt = autoFillStrategy.chooseTrackIdForAutofill(now);
    if (trackIdOpt.isEmpty()) {
      return false;
    }

    scheduleEntryService.appendTrackToTail(trackIdOpt.get());
    return true;
  }

  private AdminScheduleSlotDto toAdminSlotDto(
      ScheduleEntryDto se, Map<Long, String> playlistNamesCache) {
    var zone = radioTimeConfig.getRadioZoneId();

    Long playlistId = se.getPlaylistId();
    String playlistName = null;

    if (playlistId != null) {
      if (playlistNamesCache != null && playlistNamesCache.containsKey(playlistId)) {
        playlistName = playlistNamesCache.get(playlistId);
      } else {
        playlistName = playlistRepository.findById(playlistId).map(Playlist::getName).orElse(null);
      }
    }

    return AdminScheduleSlotDto.builder()
        .id(se.getId())
        .trackTitle(se.getTrack().getTitle())
        .trackArtworkUrl(se.getTrack().getArtworkUrl())
        .trackScUrl(se.getTrack().getSoundcloudUrl())
        .playlistId(playlistId)
        .playlistName(playlistName)
        .startTime(OffsetDateTime.ofInstant(se.getStartTime(), zone))
        .endTime(OffsetDateTime.ofInstant(se.getEndTime(), zone))
        .build();
  }
}
