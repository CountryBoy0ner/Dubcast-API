package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.request.ParseUrlRequest;
import com.Tsimur.Dubcast.dto.response.DurationOfTrackInSecondsResponse;
import com.Tsimur.Dubcast.service.impl.ParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ParserRestController {

    private final ParserService parserService;

    @PostMapping("/track")
    public TrackDto parseTrack(@RequestBody @Valid ParseUrlRequest request) {
        return parserService.parseTracksByUrl(request.getUrl());
    }

    @PostMapping("/duration")
    public DurationOfTrackInSecondsResponse getDuration(@RequestBody @Valid ParseUrlRequest request) {
        Integer seconds = parserService.getDurationSecondsByUrl(request.getUrl());
        return new DurationOfTrackInSecondsResponse(seconds);
    }



}
