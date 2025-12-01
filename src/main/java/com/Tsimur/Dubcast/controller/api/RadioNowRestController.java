package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.NowPlayingResponse;
import com.Tsimur.Dubcast.radio.NowPlayingResponseFactory;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/radio")
@RequiredArgsConstructor
public class RadioNowRestController {

    private final ScheduleEntryService scheduleEntryService;
    private final NowPlayingResponseFactory nowPlayingResponseFactory;

    @GetMapping("/now")
    public ResponseEntity<NowPlayingResponse> getNow() {
        Optional<ScheduleEntryDto> opt =
                scheduleEntryService.getCurrent(OffsetDateTime.now());

        if (opt.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        NowPlayingResponse dto = nowPlayingResponseFactory.fromScheduleEntry(opt.get());
        return ResponseEntity.ok(dto);
    }
}
