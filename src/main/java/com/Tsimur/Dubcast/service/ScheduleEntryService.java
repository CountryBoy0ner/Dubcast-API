package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleEntryService {

    ScheduleEntryDto create(ScheduleEntryDto dto);

    ScheduleEntryDto getById(Long id);

    List<ScheduleEntryDto> getAll();

    ScheduleEntryDto update(Long id, ScheduleEntryDto dto);

    void delete(Long id);


    Optional<ScheduleEntryDto> getCurrent(OffsetDateTime now);

    Optional<ScheduleEntryDto> getNext(OffsetDateTime now);

    Optional<ScheduleEntryDto> getPrevious(OffsetDateTime now);


    List<ScheduleEntryDto> getRange(OffsetDateTime from, OffsetDateTime to);

    List<ScheduleEntryDto> getDay(LocalDate date);


    Page<ScheduleEntryDto> getRangePage(OffsetDateTime from, OffsetDateTime to, Pageable pageable);

    Page<ScheduleEntryDto> getDayPage(LocalDate date, Pageable pageable);


    ScheduleEntryDto appendTrackToTail(Long id);


    void deleteSlotAndRebuildDay(Long slotId);

    ScheduleEntryDto insertTrackIntoDay(LocalDate date, Long trackId, int position);

    ScheduleEntryDto changeTrackInSlot(Long slotId, Long newTrackId);

    void reorderDay(LocalDate date, List<Long> orderedIds);

    List<ScheduleEntryDto> appendPlaylistToTail(Long playlistId);





}
