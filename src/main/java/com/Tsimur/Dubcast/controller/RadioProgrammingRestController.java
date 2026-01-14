package com.Tsimur.Dubcast.controller;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.PROGRAMMING)
@RequiredArgsConstructor
@Tag(
    name = "Radio Programming (Public)",
    description = "Read-only endpoints to inspect radio schedule around the current time.")
public class RadioProgrammingRestController {

  private final RadioProgrammingService radioProgrammingService;
  private final ScheduleEntryService scheduleEntryService;

  @GetMapping("/range")
  @Operation(
      summary = "Get schedule entries in a time range",
      description =
          """
                    Returns schedule entries that intersect with the given time range.
                    Time parameters must be in ISO-8601 format with offset, for example:
                    2026-01-13T10:00:00+02:00
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Schedule entries for the given time range",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(schema = @Schema(implementation = ScheduleEntryDto.class)))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date/time format",
            content = @Content)
      })
  public ResponseEntity<List<ScheduleEntryDto>> getRange(
      @RequestParam("from")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          @Parameter(
              description = "Start of the time range (inclusive), ISO-8601 with offset",
              example = "2026-01-13T10:00:00+02:00")
          OffsetDateTime from,
      @RequestParam("to")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          @Parameter(
              description = "End of the time range (exclusive), ISO-8601 with offset",
              example = "2026-01-13T12:00:00+02:00")
          OffsetDateTime to) {

    return ResponseEntity.ok(scheduleEntryService.getRange(from, to));
  }

  @GetMapping("/current")
  @Operation(
      summary = "Get current schedule slot",
      description =
          """
                    Returns the schedule entry (track/slot) that is active at the current time.
                    If there is no active slot, the API responds with HTTP 204 No Content.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Current schedule slot found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "204",
            description = "No current schedule slot (nothing is scheduled right now)",
            content = @Content)
      })
  public ResponseEntity<ScheduleEntryDto> getCurrent() {
    return radioProgrammingService
        .getCurrentSlot(OffsetDateTime.now())
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/next")
  @Operation(
      summary = "Get next schedule slot",
      description =
          """
                    Returns the next schedule entry after the current time.
                    If there is no upcoming slot, the API responds with HTTP 204 No Content.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Next schedule slot found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "204",
            description = "No upcoming schedule slot",
            content = @Content)
      })
  public ResponseEntity<ScheduleEntryDto> getNext() {
    return radioProgrammingService
        .getNextSlot(OffsetDateTime.now())
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }

  @GetMapping("/previous")
  @Operation(
      summary = "Get previous schedule slot",
      description =
          """
                    Returns the previous schedule entry before the current time.
                    If there is no previous slot, the API responds with HTTP 204 No Content.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Previous schedule slot found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "204",
            description = "No previous schedule slot",
            content = @Content)
      })
  public ResponseEntity<ScheduleEntryDto> getPrevious() {
    return radioProgrammingService
        .getPreviousSlot(OffsetDateTime.now())
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.noContent().build());
  }
}
