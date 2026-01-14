package com.Tsimur.Dubcast.controller;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.SCHEDULE)
@Tag(
    name = "Schedule entries (Admin)",
    description =
        "CRUD endpoints for managing radio schedule entries. Typically used from admin tools.")
@SecurityRequirement(name = "bearerAuth")
public class ScheduleEntryRestController {

  private final ScheduleEntryService scheduleEntryService;

  // --- CRUD ---

  @PostMapping
  @Operation(
      summary = "Create schedule entry",
      description =
          """
                    Creates a new schedule entry (track slot in the radio timeline).
                    Only accessible for ADMIN users.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "Schedule entry created",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (no or invalid token)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (user is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<ScheduleEntryDto> create(@Valid @RequestBody ScheduleEntryDto dto) {
    ScheduleEntryDto created = scheduleEntryService.create(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  // /api/schedule/10  (digits only, so it does not conflict with /day and /range)
  @GetMapping("/{id:\\d+}")
  @Operation(
      summary = "Get schedule entry by ID",
      description =
          """
                    Returns a single schedule entry by its identifier.
                    Only accessible for ADMIN users.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Schedule entry found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Schedule entry not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<ScheduleEntryDto> getById(
      @PathVariable @Parameter(description = "Schedule entry ID", example = "10") Long id) {
    return ResponseEntity.ok(scheduleEntryService.getById(id));
  }

  // /api/schedule  -> all entries
  @GetMapping
  @Operation(
      summary = "Get all schedule entries",
      description =
          """
                    Returns the full list of schedule entries.
                    Intended for admin tools / back-office operations, not for end users.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of schedule entries",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(schema = @Schema(implementation = ScheduleEntryDto.class)))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<List<ScheduleEntryDto>> getAll() {
    return ResponseEntity.ok(scheduleEntryService.getAll());
  }

  @PutMapping("/{id:\\d+}")
  @Operation(
      summary = "Update schedule entry",
      description =
          """
                    Updates an existing schedule entry.
                    Only accessible for ADMIN users.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Schedule entry updated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ScheduleEntryDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Schedule entry not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<ScheduleEntryDto> update(
      @PathVariable @Parameter(description = "Schedule entry ID", example = "10") Long id,
      @Valid @RequestBody ScheduleEntryDto dto) {
    ScheduleEntryDto updated = scheduleEntryService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id:\\d+}")
  @Operation(
      summary = "Delete schedule entry",
      description =
          """
                    Deletes a schedule entry by ID.
                    Only accessible for ADMIN users.
                    """,
      responses = {
        @ApiResponse(responseCode = "204", description = "Schedule entry deleted"),
        @ApiResponse(
            responseCode = "404",
            description = "Schedule entry not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> delete(
      @PathVariable @Parameter(description = "Schedule entry ID", example = "10") Long id) {
    scheduleEntryService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/day")
  @Operation(
      summary = "Get schedule for a day",
      description =
          """
                    Returns all schedule entries for a specific calendar day.
                    Date parameter is expected in ISO format, e.g. 2025-12-07.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Day schedule entries",
            content =
                @Content(
                    mediaType = "application/json",
                    array =
                        @ArraySchema(schema = @Schema(implementation = ScheduleEntryDto.class)))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date format",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<List<ScheduleEntryDto>> getDay(
      @RequestParam("date")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          @Parameter(
              description = "Day for which to return schedule, ISO date",
              example = "2025-12-07")
          LocalDate date) {
    return ResponseEntity.ok(scheduleEntryService.getDay(date));
  }
}
