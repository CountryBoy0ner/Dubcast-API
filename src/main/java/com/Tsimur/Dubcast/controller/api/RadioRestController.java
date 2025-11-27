package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.response.NowPlayingMessageResponse;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/radio")//todo nado li ?
@RequiredArgsConstructor
public class RadioRestController {

    private final ScheduleEntryService scheduleEntryService;

    @GetMapping("/now")
    public ResponseEntity<NowPlayingMessageResponse> now() {
        var now = OffsetDateTime.now();

        return scheduleEntryService.getCurrent(now)
                .map(entry -> ResponseEntity.ok(
                        NowPlayingMessageResponse.from(entry)
                ))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
