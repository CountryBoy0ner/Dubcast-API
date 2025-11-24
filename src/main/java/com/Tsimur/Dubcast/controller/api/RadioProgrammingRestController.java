package com.Tsimur.Dubcast.controller.api;


import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/programming")
@RequiredArgsConstructor
public class RadioProgrammingRestController {

    private final RadioProgrammingService radioProgrammingService;

    @PostMapping("/tracks/from-url")
    public TrackDto createTrackFromUrl(@RequestBody CreateTrackFromUrlRequest request) {
        return radioProgrammingService.createTrackFromUrl(request.soundcloudUrl());
    }

    public record CreateTrackFromUrlRequest(
            @NotBlank String soundcloudUrl
    ) {}
}
