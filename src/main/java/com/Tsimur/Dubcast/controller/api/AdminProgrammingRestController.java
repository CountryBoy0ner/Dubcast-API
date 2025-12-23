package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.ADMIN_PROGRAMMING)
@RequiredArgsConstructor
@Tag(
    name = "Admin Programming",
    description = "Admin-only endpoints for managing radio schedule and playlists.")
@SecurityRequirement(name = "bearerAuth")
public class AdminProgrammingRestController {

  private final RadioProgrammingService radioProgrammingService;

  @DeleteMapping("/slots/{id}")
  @Operation(
      summary = "Delete a schedule slot",
      description = "Deletes a schedule slot by its ID and rebuilds the schedule for that day.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Slot deleted"),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Slot not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Slot is currently playing and cannot be deleted",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> deleteSlot(
      @Parameter(description = "Schedule slot ID", example = "123") @PathVariable Long id) {
    radioProgrammingService.deleteSlotAndRebuildDay(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/day/{date}/insert-track")
  @Operation(
      summary = "Insert a track into a day schedule",
      description =
          """
                    Inserts a track into the schedule of a specific day at a given position.
                    Position is zero-based (0 — at the beginning, 1 — after the first slot, etc.).
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Track inserted into schedule",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminScheduleSlotDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters (e.g. position out of range)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Track or day not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<AdminScheduleSlotDto> insertTrackIntoDay(
      @Parameter(description = "Date of the schedule (yyyy-MM-dd)", example = "2025-12-08")
          @PathVariable
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @Parameter(description = "Track ID to insert", example = "42") @RequestParam Long trackId,
      @Parameter(description = "Zero-based position in the day schedule", example = "0")
          @RequestParam(defaultValue = "0")
          int position) {
    AdminScheduleSlotDto dto = radioProgrammingService.insertTrackIntoDay(date, trackId, position);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/slots/{id}/change-track")
  @Operation(
      summary = "Change track in an existing slot",
      description = "Replaces the current track in a schedule slot with another track.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Slot updated with new track",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminScheduleSlotDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid parameters",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Slot or track not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<AdminScheduleSlotDto> changeTrack(
      @Parameter(description = "Schedule slot ID", example = "123") @PathVariable Long id,
      @Parameter(description = "New track ID", example = "99") @RequestParam Long trackId) {
    AdminScheduleSlotDto dto = radioProgrammingService.changeTrackInSlot(id, trackId);
    return ResponseEntity.ok(dto);
  }

  @PutMapping("/day/{date}/reorder")
  @Operation(
      summary = "Reorder day schedule slots",
      description =
          """
                    Reorders all slots for a given day according to the provided list of slot IDs.
                    All IDs must belong to that specific day.
                    """,
      responses = {
        @ApiResponse(responseCode = "204", description = "Day schedule reordered"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid slot ID list",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Day or one of the slots not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> reorderDay(
      @Parameter(description = "Date of the schedule (yyyy-MM-dd)", example = "2025-12-08")
          @PathVariable
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @Parameter(description = "List of slot IDs in the desired order", example = "[10, 11, 5, 7]")
          @RequestBody
          List<Long> orderedIds) {
    radioProgrammingService.reorderDay(date, orderedIds);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/playlists/{playlistId}/append")
  @Operation(
      summary = "Append playlist to global schedule",
      description =
          "Appends all tracks from the specified playlist to the end of the current radio schedule.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Playlist appended to schedule",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PlaylistScheduleResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Playlist not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<PlaylistScheduleResponse> appendPlaylistToSchedule(
      @Parameter(description = "Playlist ID", example = "5") @PathVariable Long playlistId) {
    PlaylistScheduleResponse response =
        radioProgrammingService.appendPlaylistToSchedule(playlistId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/tracks/{trackId}/append")
  @Operation(
      summary = "Append a single track to global schedule",
      description = "Appends a single track to the end of the current radio schedule.",
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Track appended to schedule",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Track not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<ScheduleEntryDto> appendTrackToSchedule(
      @Parameter(description = "Track ID", example = "42") @PathVariable Long trackId) {
    ScheduleEntryDto dto = radioProgrammingService.appendTrackToSchedule(trackId);
    return ResponseEntity.status(201).body(dto);
  }

  @GetMapping("/day")
  @Operation(
      summary = "Get admin day schedule",
      description =
          """
                    Returns a paginated list of schedule slots for a given date.
                    This endpoint is intended for the admin panel to view and manage the radio schedule.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Day schedule returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminScheduleSlotDto.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (ADMIN role required)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Page<AdminScheduleSlotDto>> getDaySchedule(
      @Parameter(description = "Date of the schedule (yyyy-MM-dd)", example = "2025-12-08")
          @RequestParam("date")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate date,
      @Parameter(description = "Pagination and sorting parameters")
          @PageableDefault(size = 50, sort = "startTime")
          Pageable pageable) {
    Page<AdminScheduleSlotDto> page = radioProgrammingService.getDaySchedule(date, pageable);
    return ResponseEntity.ok(page);
  }
}
