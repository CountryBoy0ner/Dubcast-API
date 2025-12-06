package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


public interface RadioProgrammingService { //orchestrator
    Optional<ScheduleEntryDto> getCurrentSlot(OffsetDateTime now);

    Optional<ScheduleEntryDto> getNextSlot(OffsetDateTime now);

    Optional<ScheduleEntryDto> getPreviousSlot(OffsetDateTime now);


    PlaylistScheduleResponse appendPlaylistToSchedule(Long playlistId);

    ScheduleEntryDto appendTrackToSchedule(Long trackId);

    Page<AdminScheduleSlotDto> getDaySchedule(LocalDate date, Pageable pageable);


    void deleteSlotAndRebuildDay(Long slotId);

    AdminScheduleSlotDto insertTrackIntoDay(LocalDate date,
                                            Long trackId,
                                            int position);


    AdminScheduleSlotDto changeTrackInSlot(Long slotId, Long newTrackId);

    void reorderDay(LocalDate date, List<Long> orderedIds);

    boolean ensureAutofillIfNeeded(OffsetDateTime now);
}
