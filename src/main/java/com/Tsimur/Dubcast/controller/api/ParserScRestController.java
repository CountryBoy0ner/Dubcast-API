package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.dto.response.DurationOfTrackInSecondsResponse;
import com.Tsimur.Dubcast.dto.response.OembedHtmlResponse;
import com.Tsimur.Dubcast.service.ParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ParserScRestController {

    private final ParserService parserService;


    @PostMapping("/duration")
    public DurationOfTrackInSecondsResponse getDuration(@RequestBody @Valid UrlRequest request) {
        Integer seconds = parserService.getDurationSecondsByUrl(request.getUrl());
        return new DurationOfTrackInSecondsResponse(seconds);
    }


    @PostMapping("/playlist")
    public ResponseEntity<List<TrackDto>> parsePlaylist(@RequestBody @Valid UrlRequest request) {
        List<TrackDto> tracks = parserService.parsePlaylistByUrl(request.getUrl());
        return ResponseEntity.ok(tracks);
    }

    @PostMapping("/embedHtml")
    public OembedHtmlResponse embedHtml(@RequestBody @Valid UrlRequest request) {
        return new OembedHtmlResponse(parserService.fetchOEmbedHtml(request.getUrl()));
    }
}
