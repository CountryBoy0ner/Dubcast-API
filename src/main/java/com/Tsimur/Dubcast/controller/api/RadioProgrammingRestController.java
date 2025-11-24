package com.Tsimur.Dubcast.controller.api;


import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.request.TrackScheduleNowRequest;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import com.Tsimur.Dubcast.service.TrackService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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

    @PostMapping("/tracks/from-url")
    public ResponseEntity<TrackDto> createTrackFromUrl(@RequestBody @Valid UrlRequest request) {
        TrackDto track = radioProgrammingService.createTrackFromUrl(request.getUrl());
        return ResponseEntity.ok(track);
    }

    /**
     * 2) Импортировать трек по URL И сразу поставить в эфир "сейчас/в конец очереди"
     */
    @PostMapping("/schedule/now-from-url")
    public ResponseEntity<ScheduleEntryDto> createTrackFromUrlAndScheduleNow(
            @RequestBody @Valid UrlRequest request
    ) {
        ScheduleEntryDto entry =
                radioProgrammingService.createTrackFromUrlAndScheduleNow(request.getUrl());
        return ResponseEntity.ok(entry);
    }

    /**
     * 3) Поставить уже существующий трек (по id из БД) в эфир сейчас / в конец очереди
     */
    @PostMapping("/schedule/now")
    public ResponseEntity<ScheduleEntryDto> scheduleExistingTrackNow(
            @RequestBody @Valid TrackScheduleNowRequest request
    ) {
        ScheduleEntryDto entry =
                radioProgrammingService.scheduleExistingTrackNow(request.getTrackId());
        return ResponseEntity.ok(entry);
    }

    /**
     * 4) Что играет сейчас
     */
    @GetMapping("/current")
    public ResponseEntity<ScheduleEntryDto> getCurrent() {
        Optional<ScheduleEntryDto> current =
                scheduleEntryService.getCurrent(OffsetDateTime.now());
        return current
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * 5) Следующий трек
     */
    @GetMapping("/next")
    public ResponseEntity<ScheduleEntryDto> getNext() {
        Optional<ScheduleEntryDto> next =
                scheduleEntryService.getNext(OffsetDateTime.now());
        return next
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
