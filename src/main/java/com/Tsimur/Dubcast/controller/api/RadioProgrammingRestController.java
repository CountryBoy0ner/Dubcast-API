package com.Tsimur.Dubcast.controller.api;


import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.request.TrackScheduleNowRequest;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.radio.events.ScheduleUpdatedEvent;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;



    @Deprecated
    @PostMapping("/schedule/now-from-url")
    public ResponseEntity<ScheduleEntryDto> createTrackFromUrlAndScheduleNow(
            @RequestBody @Valid UrlRequest request
    ) {
        ScheduleEntryDto entry = radioProgrammingService.createTrackFromUrlAndScheduleNow(request.getUrl());
        eventPublisher.publishEvent(new ScheduleUpdatedEvent(OffsetDateTime.now()));

        return ResponseEntity.ok(entry);
    }
    @Deprecated
    @PostMapping("/schedule/now")
    public ResponseEntity<ScheduleEntryDto> scheduleExistingTrackNow(
            @RequestBody @Valid TrackScheduleNowRequest request
    ) {
        ScheduleEntryDto entry =
                radioProgrammingService.scheduleExistingTrackNow(request.getTrackId());
        return ResponseEntity.ok(entry);
    }

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
