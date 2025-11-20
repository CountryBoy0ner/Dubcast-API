package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")
public class ScheduleEntryRestController {

    private final ScheduleEntryService scheduleEntryService;

    @PostMapping
    public ResponseEntity<ScheduleEntryDto> create(@Valid @RequestBody ScheduleEntryDto dto) {
        ScheduleEntryDto created = scheduleEntryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ScheduleEntryDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(scheduleEntryService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<ScheduleEntryDto>> getAll() {
        return ResponseEntity.ok(scheduleEntryService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ScheduleEntryDto> update(@PathVariable Long id,
                                                   @Valid @RequestBody ScheduleEntryDto dto) {
        ScheduleEntryDto updated = scheduleEntryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        scheduleEntryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
