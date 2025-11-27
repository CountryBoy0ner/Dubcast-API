package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.exception.type.ScheduleOverlapException;
import com.Tsimur.Dubcast.mapper.ScheduleEntryMapper;
import com.Tsimur.Dubcast.model.ScheduleEntry;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleEntryServiceImpl implements ScheduleEntryService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;
    private final TrackRepository trackRepository;

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
            throw  NotFoundException.of("Schedule", "id", id);
        }
        scheduleEntryRepository.deleteById(id);
    }

    @Override
    public ScheduleEntryDto scheduleAt(Long trackId, OffsetDateTime startTime) {
        Track track = trackRepository.findById(trackId)
                .orElseThrow(() -> new NotFoundException("Track not found"));

        OffsetDateTime endTime = startTime.plusSeconds(track.getDurationSeconds());

        boolean overlaps = scheduleEntryRepository.existsOverlap(startTime, endTime);
        if (overlaps) {
            throw new ScheduleOverlapException("Time slot is already taken"); //todo
        }

        ScheduleEntry entry = ScheduleEntry.builder()
                .track(track)
                .startTime(startTime)
                .endTime(endTime)
                .createdAt(OffsetDateTime.now())
                .build();

        return scheduleEntryMapper.toDto(scheduleEntryRepository.save(entry));
    }


    @Override
    public ScheduleEntryDto scheduleNow(Long trackId) {
        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime startTime = scheduleEntryRepository.findLastEndTimeAfter(now)
                .orElse(now);

        return scheduleAt(trackId, startTime);
    }


    @Override
    public Optional<ScheduleEntryDto> getCurrent(OffsetDateTime now) {
        return scheduleEntryRepository.findCurrent(now).map(scheduleEntryMapper::toDto);
    }

    @Override
    public Optional<ScheduleEntryDto> getNext(OffsetDateTime now) {
        return scheduleEntryRepository.findNext(now, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(scheduleEntryMapper::toDto);
    }

}
