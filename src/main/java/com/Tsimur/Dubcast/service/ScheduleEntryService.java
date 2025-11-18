package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;

import java.util.List;

public interface ScheduleEntryService {

    ScheduleEntryDto create(ScheduleEntryDto dto);

    ScheduleEntryDto getById(Long id);

    List<ScheduleEntryDto> getAll();

    ScheduleEntryDto update(Long id, ScheduleEntryDto dto);

    void delete(Long id);
}
