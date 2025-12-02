package com.Tsimur.Dubcast.controller.api;


import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/programming")
@RequiredArgsConstructor
public class RadioProgrammingRestController {

    private final RadioProgrammingService radioProgrammingService;
    private final ScheduleEntryService scheduleEntryService;


    @GetMapping("/current")
    public ResponseEntity<ScheduleEntryDto> getCurrent() {
        Optional<ScheduleEntryDto> current =
                scheduleEntryService.getCurrent(OffsetDateTime.now());
        return current
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/next")
    public ResponseEntity<ScheduleEntryDto> getNext() {
        Optional<ScheduleEntryDto> next =
                scheduleEntryService.getNext(OffsetDateTime.now());
        return next
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/playlists/{playlistId}/append")
    public ResponseEntity<PlaylistScheduleResponse> appendPlaylistToSchedule(@PathVariable Long playlistId) {
        PlaylistScheduleResponse response = radioProgrammingService.appendPlaylistToSchedule(playlistId);
        return ResponseEntity.ok(response);
    }


}
