package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.service.ParserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.PARSER)
@RequiredArgsConstructor
@Tag(name = "Parser", description = "Endpoints for parsing SoundCloud tracks and playlists by URL.")
public class ParserScRestController {

  private final ParserService parserService;

  @PostMapping("/track")
  @Operation(
      summary = "Parse a single SoundCloud track",
      description = "Accepts a SoundCloud track URL and returns parsed track metadata.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Track was successfully parsed.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TrackDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid URL format or unsupported resource.",
        content = @Content),
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error while contacting SoundCloud or parsing the track.",
        content = @Content)
  })
  public TrackDto parseTrack(@RequestBody @Valid UrlRequest request) {
    return parserService.parseTracksByUrl(request.getUrl());
  }

  @PostMapping("/playlist")
  @Operation(
      summary = "Parse a SoundCloud playlist",
      description =
          "Accepts a SoundCloud playlist URL and returns metadata for all tracks in the playlist.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Playlist was successfully parsed.",
        content =
            @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = TrackDto.class)))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid URL format or unsupported resource.",
        content = @Content),
    @ApiResponse(
        responseCode = "500",
        description =
            "Unexpected server error while contacting SoundCloud or parsing the playlist.",
        content = @Content)
  })
  public List<TrackDto> parsePlaylist(@RequestBody @Valid UrlRequest request) {
    return parserService.parsePlaylistByUrl(request.getUrl());
  }

  @PostMapping("/duration")
  @Operation(
      summary = "Get track duration in seconds",
      description = "Accepts a SoundCloud track URL and returns its duration in seconds.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Duration was successfully calculated.",
        content =
            @Content(
                mediaType = "application/json",
                schema =
                    @Schema(
                        implementation = Integer.class,
                        description = "Duration of the track in seconds."))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid URL format or unsupported resource.",
        content = @Content),
    @ApiResponse(
        responseCode = "404",
        description = "Track not found or duration could not be determined.",
        content = @Content),
    @ApiResponse(
        responseCode = "500",
        description = "Unexpected server error while contacting SoundCloud or parsing metadata.",
        content = @Content)
  })
  public ResponseEntity<Integer> getDuration(@RequestBody @Valid UrlRequest request) {
    Integer seconds = parserService.getDurationSecondsByUrl(request.getUrl());
    return ResponseEntity.ok(seconds);
  }
}
