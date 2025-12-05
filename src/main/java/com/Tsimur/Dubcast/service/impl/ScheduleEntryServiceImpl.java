package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.config.RadioTimeConfig;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.ScheduleEntryMapper;
import com.Tsimur.Dubcast.model.ScheduleEntry;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleEntryServiceImpl implements ScheduleEntryService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;
    private final TrackRepository trackRepository;
    private final RadioTimeConfig radioTimeConfig;


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


    @Override
    public Optional<ScheduleEntryDto> getCurrent(OffsetDateTime now) { //todo добавить exception if null(подумать надо ли добавлять вообщзе)
        return scheduleEntryRepository.findCurrent(now).map(scheduleEntryMapper::toDto);
    }

    @Override
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
        OffsetDateTime to   = from.plusDays(1);
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
        OffsetDateTime to   = from.plusDays(1);
        return getRangePage(from, to, pageable);
    }
}
