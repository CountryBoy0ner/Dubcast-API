package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleEntryService {

    ScheduleEntryDto create(ScheduleEntryDto dto);

    ScheduleEntryDto getById(Long id);

    List<ScheduleEntryDto> getAll();

    ScheduleEntryDto update(Long id, ScheduleEntryDto dto);

    void delete(Long id);

    @Deprecated
        // TODO: delete after implementing Playlists
    ScheduleEntryDto scheduleAt(Long trackId, OffsetDateTime startTime);

    @Deprecated
        // TODO: delete after implementing Playlists
    ScheduleEntryDto scheduleNow(Long trackId);

    Optional<ScheduleEntryDto> getCurrent(OffsetDateTime now);

    Optional<ScheduleEntryDto> getNext(OffsetDateTime now);


}
