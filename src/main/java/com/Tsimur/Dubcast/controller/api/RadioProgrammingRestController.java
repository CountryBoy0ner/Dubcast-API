package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
