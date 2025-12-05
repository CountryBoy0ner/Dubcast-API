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
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/programming")
@RequiredArgsConstructor
public class RadioProgrammingRestController {

    private final RadioProgrammingService radioProgrammingService;

    @GetMapping("/current")
    public ResponseEntity<ScheduleEntryDto> getCurrent() {
        return radioProgrammingService.getCurrentSlot(OffsetDateTime.now())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/next")
    public ResponseEntity<ScheduleEntryDto> getNext() {
        return radioProgrammingService.getNextSlot(OffsetDateTime.now())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/previous")
    public ResponseEntity<ScheduleEntryDto> getPrevious() {
        return radioProgrammingService.getPreviousSlot(OffsetDateTime.now())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
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
