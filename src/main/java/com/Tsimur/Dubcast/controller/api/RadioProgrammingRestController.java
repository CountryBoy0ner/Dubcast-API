package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
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
