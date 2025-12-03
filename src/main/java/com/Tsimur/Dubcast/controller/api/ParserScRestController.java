package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.service.ParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ParserScRestController {

    private final ParserService parserService;

    @PostMapping("/track")
    public TrackDto parseTrack(@RequestBody UrlRequest request) {
        return parserService.parseTracksByUrl(request.getUrl());
    }

    @PostMapping("/playlist")
    public List<TrackDto> parsePlaylist(@RequestBody UrlRequest request) {
        return parserService.parsePlaylistByUrl(request.getUrl());
    }

    @PostMapping("/duration")
    public ResponseEntity<Integer> getDuration(@RequestBody UrlRequest request) {
        Integer seconds = parserService.getDurationSecondsByUrl(request.getUrl());
        // if (seconds == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(seconds);
    }


}
