package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleEntryRestController {

    private final ScheduleEntryService scheduleEntryService;

    // --- CRUD ---

    @PostMapping
    public ResponseEntity<ScheduleEntryDto> create(@Valid @RequestBody ScheduleEntryDto dto) {
        ScheduleEntryDto created = scheduleEntryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // /api/schedule/10  (только цифры, чтобы не конфликтовать с /day и /range)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ScheduleEntryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleEntryService.getById(id));
    }

    // /api/schedule  -> все записи
    @GetMapping
    public ResponseEntity<List<ScheduleEntryDto>> getAll() {
        return ResponseEntity.ok(scheduleEntryService.getAll());
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<ScheduleEntryDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody ScheduleEntryDto dto) {
        ScheduleEntryDto updated = scheduleEntryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/range")
    public ResponseEntity<List<ScheduleEntryDto>> getRange(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,

            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to
    ) {
        return ResponseEntity.ok(scheduleEntryService.getRange(from, to));
    }

    @GetMapping("/day")
    public ResponseEntity<List<ScheduleEntryDto>> getDay(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return ResponseEntity.ok(scheduleEntryService.getDay(date));
    }
}
