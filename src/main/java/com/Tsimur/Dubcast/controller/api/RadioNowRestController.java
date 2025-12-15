package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.NowPlayingResponse;
import com.Tsimur.Dubcast.radio.NowPlayingResponseFactory;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping(ApiPaths.RADIO)
@RequiredArgsConstructor
@Tag(
        name = "Radio",
        description = "Public endpoints for reading current radio state."
)
public class RadioNowRestController {

    private final ScheduleEntryService scheduleEntryService;
    private final NowPlayingResponseFactory nowPlayingResponseFactory;

    @GetMapping("/now")
    @Operation(
            summary = "Get currently playing track",
            description = """
                    Returns information about the track that is currently playing on the radio.
                    If there is no scheduled track at the current time, the API responds with HTTP 204 No Content.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "A track is currently playing",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = NowPlayingResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "204",
                            description = "Nothing is playing right now (no scheduled entry for the current time)",
                            content = @Content
                    )
            }
    )
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
