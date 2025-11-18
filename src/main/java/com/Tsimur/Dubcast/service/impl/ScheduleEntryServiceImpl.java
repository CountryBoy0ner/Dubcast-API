package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.mapper.ScheduleEntryMapper;
import com.Tsimur.Dubcast.model.ScheduleEntry;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleEntryServiceImpl implements ScheduleEntryService {

    private final ScheduleEntryRepository scheduleEntryRepository;
    private final ScheduleEntryMapper scheduleEntryMapper;

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
                .orElseThrow(() -> new EntityNotFoundException("ScheduleEntry not found: " + id));
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
                .orElseThrow(() -> new EntityNotFoundException("ScheduleEntry not found: " + id));

        scheduleEntryMapper.updateEntityFromDto(dto, existing);

        ScheduleEntry saved = scheduleEntryRepository.save(existing);
        return scheduleEntryMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!scheduleEntryRepository.existsById(id)) {
            throw new EntityNotFoundException("ScheduleEntry not found: " + id);
        }
        scheduleEntryRepository.deleteById(id);
    }
}
