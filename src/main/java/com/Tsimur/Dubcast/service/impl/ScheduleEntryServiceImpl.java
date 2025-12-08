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
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleEntryServiceImpl implements ScheduleEntryService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;
    private final TrackRepository trackRepository;
    private final RadioTimeConfig radioTimeConfig;
    private final ApplicationEventPublisher eventPublisher;

    private final PlaylistRepository playlistRepository;          // <-- NEW
    private final PlaylistTrackRepository playlistTrackRepository; // <-- NEW

    // ------------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------------
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
        ScheduleEntry entity = scheduleEntryRepository.findById(id)
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
        ScheduleEntry existing = scheduleEntryRepository.findById(id)
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

    // ------------------------------------------------------------------------
    // Поиск текущего / следующего / предыдущего слота
    // ------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleEntryDto> getCurrent(OffsetDateTime now) {
        return scheduleEntryRepository.findCurrent(now).map(scheduleEntryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleEntryDto> getNext(OffsetDateTime now) {
        return scheduleEntryRepository.findNext(now, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(scheduleEntryMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ScheduleEntryDto> getPrevious(OffsetDateTime now) {
        return scheduleEntryRepository.findPrevious(now, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(scheduleEntryMapper::toDto);
    }

    // ------------------------------------------------------------------------
    // Диапазоны / день
    // ------------------------------------------------------------------------
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
    public Page<ScheduleEntryDto> getRangePage(OffsetDateTime from, OffsetDateTime to, Pageable pageable) {
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

    // ------------------------------------------------------------------------
    // Добавить трек в хвост расписания
    // ------------------------------------------------------------------------
    @Override
    public ScheduleEntryDto appendTrackToTail(Long trackId) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> NotFoundException.of("Track", "id", trackId));

        Integer duration = track.getDurationSeconds();
        if (duration == null || duration <= 0) {
            throw new IllegalStateException("Track " + trackId + " has no valid duration");
        }

        var zone = radioTimeConfig.getRadioZoneId();
        OffsetDateTime now = OffsetDateTime.now(zone);

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

        // уведомляем RadioClock, что расписание обновилось
        eventPublisher.publishEvent(new ScheduleUpdatedEvent(saved.getStartTime()));

        return scheduleEntryMapper.toDto(saved);
    }


    // ------------------------------------------------------------------------
    // Админские операции над днём: удалить / вставить / поменять / переупорядочить
    // ------------------------------------------------------------------------
    @Override
    @Transactional
    public void deleteSlotAndRebuildDay(Long slotId) {
        ScheduleEntry entry = scheduleEntryRepository.findById(slotId)
                .orElseThrow(() ->  NotFoundException.of("Schedule", "id" , slotId));

        var zone = radioTimeConfig.getRadioZoneId();
        OffsetDateTime now = OffsetDateTime.now(zone);

        boolean started = !now.isBefore(entry.getStartTime()); // now >= start
        boolean notFinished = now.isBefore(entry.getEndTime()); // now < end


        if (started && notFinished) {
            throw new SlotCurrentlyPlayingException(slotId);
        }

        // если слот не текущий – дальше логика как была
        LocalDate date = entry.getStartTime()
                .atZoneSameInstant(zone)
                .toLocalDate();

        scheduleEntryRepository.delete(entry);

        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<ScheduleEntry> dayEntries = scheduleEntryRepository
                .findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

        rebuildDaySchedule(date, dayEntries);
    }


    @Override
    @Transactional
    public ScheduleEntryDto insertTrackIntoDay(LocalDate date,
                                               Long trackId,
                                               int position) {

        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> NotFoundException.of("Track", "id", trackId));

        var zone = radioTimeConfig.getRadioZoneId();
        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<ScheduleEntry> dayEntries = scheduleEntryRepository
                .findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

        if (position < 0) position = 0;
        if (position > dayEntries.size()) position = dayEntries.size();

        ScheduleEntry newEntry = ScheduleEntry.builder()
                .track(track)
                .playlist(null)
                .build();

        dayEntries.add(position, newEntry);

        rebuildDaySchedule(date, dayEntries);

        return scheduleEntryMapper.toDto(newEntry);
    }

    @Override
    @Transactional
    public ScheduleEntryDto changeTrackInSlot(Long slotId, Long newTrackId) {
        ScheduleEntry entry = scheduleEntryRepository.findById(slotId)
                .orElseThrow(() -> NotFoundException.of("Schedule", "id", slotId));

        Track newTrack = trackRepository.findById(newTrackId)
                .orElseThrow(() -> NotFoundException.of("Track", "id", newTrackId));

        entry.setTrack(newTrack);

        var zone = radioTimeConfig.getRadioZoneId();
        LocalDate date = entry.getStartTime()
                .atZoneSameInstant(zone)
                .toLocalDate();

        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<ScheduleEntry> dayEntries = scheduleEntryRepository
                .findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

        rebuildDaySchedule(date, dayEntries);

        return scheduleEntryMapper.toDto(entry);
    }

    @Override
    @Transactional
    public void reorderDay(LocalDate date, List<Long> orderedIds) {
        var zone = radioTimeConfig.getRadioZoneId();
        OffsetDateTime dayStart = date.atStartOfDay(zone).toOffsetDateTime();
        OffsetDateTime dayEnd = dayStart.plusDays(1);

        List<ScheduleEntry> dayEntries = scheduleEntryRepository
                .findByStartTimeBetweenOrderByStartTime(dayStart, dayEnd);

        Map<Long, ScheduleEntry> byId = dayEntries.stream()
                .collect(Collectors.toMap(ScheduleEntry::getId, e -> e));

        List<ScheduleEntry> reordered = new ArrayList<>();

        for (Long id : orderedIds) {
            ScheduleEntry e = byId.get(id);
            if (e != null) {
                reordered.add(e);
            }
        }
        for (ScheduleEntry e : dayEntries) {
            if (!reordered.contains(e)) {
                reordered.add(e);
            }
        }

        rebuildDaySchedule(date, reordered);
    }

    @Override
    @Transactional
    public List<ScheduleEntryDto> appendPlaylistToTail(Long playlistId) {
        // 1. Берём плейлист
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new NotFoundException("Playlist not found: " + playlistId));

        // 2. Вычисляем, откуда начинать (хвост расписания или "прямо сейчас")
        var zone = radioTimeConfig.getRadioZoneId();
        OffsetDateTime now = OffsetDateTime.now(zone);
        OffsetDateTime maxEndTime = scheduleEntryRepository.findMaxEndTime();

        OffsetDateTime startTime =
                (maxEndTime == null || maxEndTime.isBefore(now))
                        ? now
                        : maxEndTime;

        // 3. Берём треки плейлиста в порядке позиций
        List<PlaylistTrack> pts =
                playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId);

        List<ScheduleEntryDto> result = new ArrayList<>();
        OffsetDateTime firstStartTime = null;

        for (PlaylistTrack pt : pts) {
            Track track = pt.getTrack();
            Integer duration = track.getDurationSeconds();
            if (duration == null || duration <= 0) {
                // пропускаем битые треки
                continue;
            }

            OffsetDateTime endTime = startTime.plusSeconds(duration);

            ScheduleEntry entry = ScheduleEntry.builder()
                    .track(track)
                    .playlist(playlist)    // важно: связываем с плейлистом
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

        // 4. Если что-то реально добавили — уведомляем радио-часы
        if (firstStartTime != null) {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(firstStartTime));
        }

        return result;
    }

    // ------------------------------------------------------------------------
    // Общий помощник: пересчитать старт/конец внутри одного дня
    // ------------------------------------------------------------------------
    private void rebuildDaySchedule(LocalDate date, List<ScheduleEntry> entries) {
        var zone = radioTimeConfig.getRadioZoneId();
        OffsetDateTime currentStart = date.atStartOfDay(zone).toOffsetDateTime();

        for (ScheduleEntry e : entries) {
            Track t = e.getTrack();
            Integer duration = t != null ? t.getDurationSeconds() : null;
            if (duration == null || duration <= 0) {
                throw new IllegalStateException(
                        "Track " + (t != null ? t.getId() : "null") + " has invalid duration");
            }

            e.setStartTime(currentStart);
            e.setEndTime(currentStart.plusSeconds(duration));
            currentStart = e.getEndTime();
        }

        scheduleEntryRepository.saveAll(entries);

        if (!entries.isEmpty()) {
            eventPublisher.publishEvent(new ScheduleUpdatedEvent(entries.get(0).getStartTime()));
        }
    }
}
