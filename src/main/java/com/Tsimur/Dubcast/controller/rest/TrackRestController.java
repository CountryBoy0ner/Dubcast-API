package com.Tsimur.Dubcast.controller.rest;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.service.TrackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracks")
public class TrackRestController {

    private final TrackService trackService;

    @PostMapping
    public ResponseEntity<TrackDto> create(@Valid @RequestBody TrackDto dto) {
        TrackDto created = trackService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrackDto> getById(@PathVariable Long id) {
        TrackDto dto = trackService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<TrackDto>> getAll() {
        return ResponseEntity.ok(trackService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackDto> update(@PathVariable Long id,
                                           @Valid @RequestBody TrackDto dto) {
        TrackDto updated = trackService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        trackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
