package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.config.RadioTimeConfig;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.exception.type.SlotCurrentlyPlayingException;
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
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleEntryServiceImpl implements ScheduleEntryService {

  private final ScheduleEntryRepository scheduleEntryRepository;
  private final ScheduleEntryMapper scheduleEntryMapper;
  private final TrackRepository trackRepository;
  private final RadioTimeConfig radioTimeConfig;
  private final ApplicationEventPublisher eventPublisher;

  private final PlaylistRepository playlistRepository;
  private final PlaylistTrackRepository playlistTrackRepository;

  @Override
  public ScheduleEntryDto create(ScheduleEntryDto dto) {
    dto.setId(null);
    ScheduleEntry entity = scheduleEntryMapper.toEntity(dto);
    ScheduleEntry saved = scheduleEntryRepository.save(entity);
    return scheduleEntryMapper.toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public ScheduleEntryDto getById(Long id) {
    ScheduleEntry entity =
        scheduleEntryRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.of("Schedule", "id", id));
    return scheduleEntryMapper.toDto(entity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ScheduleEntryDto> getAll() {
    List<ScheduleEntry> all = scheduleEntryRepository.findAll();
    return scheduleEntryMapper.toDtoList(all);
  }

  @Override
  public ScheduleEntryDto update(Long id, ScheduleEntryDto dto) {
    ScheduleEntry existing =
        scheduleEntryRepository
            .findById(id)
            .orElseThrow(() -> NotFoundException.of("Schedule", "id", id));
    scheduleEntryMapper.updateEntityFromDto(dto, existing);
    ScheduleEntry saved = scheduleEntryRepository.save(existing);
    return scheduleEntryMapper.toDto(saved);
  }

  @Override
  public void delete(Long id) {
    if (!scheduleEntryRepository.existsById(id)) {
      throw NotFoundException.of("Schedule", "id", id);
    }
    scheduleEntryRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ScheduleEntryDto> getCurrent(OffsetDateTime now) {
    return scheduleEntryRepository.findCurrent(now).map(scheduleEntryMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ScheduleEntryDto> getNext(OffsetDateTime now) {
    return scheduleEntryRepository.findNext(now, PageRequest.of(0, 1)).stream()
        .findFirst()
        .map(scheduleEntryMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<ScheduleEntryDto> getPrevious(OffsetDateTime now) {
    return scheduleEntryRepository.findPrevious(now, PageRequest.of(0, 1)).stream()
        .findFirst()
        .map(scheduleEntryMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ScheduleEntryDto> getRange(OffsetDateTime from, OffsetDateTime to) {
    List<ScheduleEntry> entries =
        scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(from, to);
    return scheduleEntryMapper.toDtoList(entries);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ScheduleEntryDto> getDay(LocalDate date) {
    ZoneId zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime from = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime to = from.plusDays(1);
    return getRange(from, to);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ScheduleEntryDto> getRangePage(
      OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
    Page<ScheduleEntry> page =
        scheduleEntryRepository.findPageByStartTimeBetweenOrderByStartTime(from, to, pageable);
    return page.map(scheduleEntryMapper::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<ScheduleEntryDto> getDayPage(LocalDate date, Pageable pageable) {
    ZoneId zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime from = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime to = from.plusDays(1);
    return getRangePage(from, to, pageable);
  }

  @Override
  public ScheduleEntryDto appendTrackToTail(Long trackId) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(() -> NotFoundException.of("Track", "id", trackId));

    Integer duration = track.getDurationSeconds();
    if (duration == null || duration <= 0) {
      throw new IllegalStateException("Track " + trackId + " has no valid duration");
    }

    var zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime now = OffsetDateTime.now(zone);

    OffsetDateTime maxEndTime = scheduleEntryRepository.findMaxEndTime();

    OffsetDateTime startTime = (maxEndTime == null || maxEndTime.isBefore(now)) ? now : maxEndTime;

    OffsetDateTime endTime = startTime.plusSeconds(duration);

    ScheduleEntry entry =
        ScheduleEntry.builder()
            .track(track)
            .playlist(null)
            .startTime(startTime)
            .endTime(endTime)
            .build();

    ScheduleEntry saved = scheduleEntryRepository.save(entry);

    eventPublisher.publishEvent(new ScheduleUpdatedEvent(saved.getStartTime()));

    return scheduleEntryMapper.toDto(saved);
  }

  @Override
  @Transactional
  public void deleteSlotAndRebuildDay(Long slotId) {
    ScheduleEntry entry =
        scheduleEntryRepository
            .findById(slotId)
            .orElseThrow(() -> NotFoundException.of("Schedule", "id", slotId));

    var zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime now = OffsetDateTime.now(zone);

    boolean started = !now.isBefore(entry.getStartTime());
    boolean notFinished = now.isBefore(entry.getEndTime());
    if (started && notFinished) {
      throw new SlotCurrentlyPlayingException(slotId);
    }

    OffsetDateTime deletedStart = entry.getStartTime();
    LocalDate date = entry.getStartTime().atZoneSameInstant(zone).toLocalDate();

    scheduleEntryRepository.delete(entry);
    scheduleEntryRepository.flush();

    OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime dayEnd = dayStart.plusDays(1);

    List<ScheduleEntry> dayEntries =
        scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

    if (dayEntries.isEmpty()) {
      eventPublisher.publishEvent(new ScheduleUpdatedEvent(deletedStart));
      return;
    }

    int fromIndex = 0;
    while (fromIndex < dayEntries.size()
        && dayEntries.get(fromIndex).getStartTime().isBefore(deletedStart)) {
      fromIndex++;
    }

    rebuildFromIndex(deletedStart, dayEntries, fromIndex);
  }

  @Override
  @Transactional
  public ScheduleEntryDto insertTrackIntoDay(LocalDate date, Long trackId, int position) {
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(() -> NotFoundException.of("Track", "id", trackId));

    var zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime dayEnd = dayStart.plusDays(1);

    List<ScheduleEntry> dayEntries =
        scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

    if (position < 0) position = 0;
    if (position > dayEntries.size()) position = dayEntries.size();

    ScheduleEntry newEntry = ScheduleEntry.builder().track(track).playlist(null).build();

    dayEntries.add(position, newEntry);

    OffsetDateTime anchorStart;
    int fromIndex;

    if (dayEntries.size() == 1) {
      anchorStart = dayStart;
      fromIndex = 0;
    } else if (position == 0) {
      anchorStart = dayEntries.get(1).getStartTime();
      fromIndex = 0;
    } else {
      anchorStart = dayEntries.get(position - 1).getEndTime();
      fromIndex = position;
    }

    rebuildFromIndex(anchorStart, dayEntries, fromIndex);

    return scheduleEntryMapper.toDto(newEntry);
  }

  @Override
  @Transactional
  public ScheduleEntryDto changeTrackInSlot(Long slotId, Long newTrackId) {
    ScheduleEntry entry =
        scheduleEntryRepository
            .findById(slotId)
            .orElseThrow(() -> NotFoundException.of("Schedule", "id", slotId));

    Track newTrack =
        trackRepository
            .findById(newTrackId)
            .orElseThrow(() -> NotFoundException.of("Track", "id", newTrackId));

    var zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime now = OffsetDateTime.now(zone);

    boolean started = !now.isBefore(entry.getStartTime());
    boolean notFinished = now.isBefore(entry.getEndTime());
    if (started && notFinished) {
      throw new SlotCurrentlyPlayingException(slotId);
    }

    entry.setTrack(newTrack);
    scheduleEntryRepository.save(entry);
    scheduleEntryRepository.flush();

    LocalDate date = entry.getStartTime().atZoneSameInstant(zone).toLocalDate();
    OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
    OffsetDateTime dayEnd = dayStart.plusDays(1);

    List<ScheduleEntry> dayEntries =
        scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

    int fromIndex = -1;
    for (int i = 0; i < dayEntries.size(); i++) {
      if (Objects.equals(dayEntries.get(i).getId(), slotId)) {
        fromIndex = i;
        break;
      }
    }
    if (fromIndex == -1) {
      throw new IllegalStateException("Slot " + slotId + " not found in dayEntries after reload");
    }

    rebuildFromIndex(entry.getStartTime(), dayEntries, fromIndex);

    ScheduleEntry refreshed =
        scheduleEntryRepository
            .findById(slotId)
            .orElseThrow(() -> NotFoundException.of("Schedule", "id", slotId));

    return scheduleEntryMapper.toDto(refreshed);
  }

  @Override
  public void reorderDay(LocalDate date, List<Long> orderedIds) {}

  @Override
  @Transactional
  public List<ScheduleEntryDto> appendPlaylistToTail(Long playlistId) {
    Playlist playlist =
        playlistRepository
            .findById(playlistId)
            .orElseThrow(() -> new NotFoundException("Playlist not found: " + playlistId));

    var zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime now = OffsetDateTime.now(zone);
    OffsetDateTime maxEndTime = scheduleEntryRepository.findMaxEndTime();

    OffsetDateTime startTime = (maxEndTime == null || maxEndTime.isBefore(now)) ? now : maxEndTime;

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

      ScheduleEntry entry =
          ScheduleEntry.builder()
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

    return result;
  }

  private void rebuildFromIndex(
      OffsetDateTime anchorStart, List<ScheduleEntry> entries, int fromIndex) {
    if (fromIndex < 0) fromIndex = 0;
    if (fromIndex >= entries.size()) {
      eventPublisher.publishEvent(new ScheduleUpdatedEvent(anchorStart));
      return;
    }

    OffsetDateTime currentStart = anchorStart;

    for (int i = fromIndex; i < entries.size(); i++) {
      ScheduleEntry e = entries.get(i);
      Track t = e.getTrack();
      Integer duration = (t != null) ? t.getDurationSeconds() : null;

      if (duration == null || duration <= 0) {
        throw new IllegalStateException(
            "Track " + (t != null ? t.getId() : "null") + " has invalid duration");
      }

      e.setStartTime(currentStart);
      e.setEndTime(currentStart.plusSeconds(duration));
      currentStart = e.getEndTime();
    }

    scheduleEntryRepository.saveAll(entries.subList(fromIndex, entries.size()));
    scheduleEntryRepository.flush();

    eventPublisher.publishEvent(new ScheduleUpdatedEvent(anchorStart));
  }
}
