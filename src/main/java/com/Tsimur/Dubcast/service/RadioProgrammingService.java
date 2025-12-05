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

    //List<ScheduleEntryDto> deliteLastSchedules(int quantity);//удаляет из запланированного расписания n последних слотов

    //PlaylistScheduleResponse deliteLastPlayingPlaylist(int quantity);//удаляет из запланированного расписания последний дбавленный плейлист (группу треков с одинаковым playlist id)

    PlaylistScheduleResponse appendPlaylistToSchedule(Long playlistId);

    ScheduleEntryDto appendTrackToSchedule(Long trackId);

    public Page<AdminScheduleSlotDto> getDaySchedule(LocalDate date, Pageable pageable);
}
