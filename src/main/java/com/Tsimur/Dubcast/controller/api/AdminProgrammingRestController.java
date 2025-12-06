package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/programming")
@RequiredArgsConstructor
public class AdminProgrammingRestController {

    private final RadioProgrammingService radioProgrammingService;

    // 1) Удалить слот + пересчитать день
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long id) {
        radioProgrammingService.deleteSlotAndRebuildDay(id);
        return ResponseEntity.noContent().build();
    }

    // 2) Вставить трек в день по позиции
    @PostMapping("/day/{date}/insert-track")
    public ResponseEntity<AdminScheduleSlotDto> insertTrackIntoDay(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam Long trackId,
            @RequestParam(defaultValue = "0") int position
    ) {
        AdminScheduleSlotDto dto = radioProgrammingService.insertTrackIntoDay(date, trackId, position);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/slots/{id}/change-track")
    public ResponseEntity<AdminScheduleSlotDto> changeTrack(
            @PathVariable Long id,
            @RequestParam Long trackId
    ) {
        AdminScheduleSlotDto dto = radioProgrammingService.changeTrackInSlot(id, trackId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/day/{date}/reorder")
    public ResponseEntity<Void> reorderDay(
            @PathVariable
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestBody List<Long> orderedIds
    ) {
        radioProgrammingService.reorderDay(date, orderedIds);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/playlists/{playlistId}/append")
    public ResponseEntity<PlaylistScheduleResponse> appendPlaylistToSchedule(@PathVariable Long playlistId) {
        PlaylistScheduleResponse response = radioProgrammingService.appendPlaylistToSchedule(playlistId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tracks/{trackId}/append")
    public ResponseEntity<ScheduleEntryDto> appendTrackToSchedule(@PathVariable Long trackId) {
        ScheduleEntryDto dto = radioProgrammingService.appendTrackToSchedule(trackId);
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping("/day")
    public ResponseEntity<Page<AdminScheduleSlotDto>> getDaySchedule(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @PageableDefault(size = 50, sort = "startTime")
            Pageable pageable
    ) {
        Page<AdminScheduleSlotDto> page = radioProgrammingService.getDaySchedule(date, pageable);
        return ResponseEntity.ok(page);
    }
}
