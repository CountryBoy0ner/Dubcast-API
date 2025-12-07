package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.service.TrackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tracks")
@Tag(
        name = "Tracks (Admin)",
        description = "CRUD endpoints for managing tracks in the system. Intended for admin/back-office usage."
)
@SecurityRequirement(name = "bearerAuth")
public class TrackRestController {

    private final TrackService trackService;

    @PostMapping
    @Operation(
            summary = "Create track",
            description = """
                    Creates a new track in the catalog.
                    Typically used from admin tools or import flows.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Track created",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TrackDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error in request body",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized (no or invalid JWT)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden (user is not an admin)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<TrackDto> create(
            @Valid @RequestBody
            @Parameter(description = "Track payload to create")
            TrackDto dto
    ) {
        TrackDto created = trackService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get track by ID",
            description = """
                    Returns a single track by its identifier.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Track found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TrackDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Track not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<TrackDto> getById(
            @PathVariable
            @Parameter(description = "Track ID", example = "42")
            Long id
    ) {
        TrackDto dto = trackService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    @Operation(
            summary = "Get all tracks",
            description = """
                    Returns the full list of tracks in the catalog.
                    Can be used in admin UI to browse or search tracks.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of tracks",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TrackDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<List<TrackDto>> getAll() {
        return ResponseEntity.ok(trackService.getAll());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update track",
            description = """
                    Updates an existing track by ID.
                    All updatable fields are taken from the request body.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Track updated",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TrackDto.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Validation error in request body",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Track not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<TrackDto> update(
            @PathVariable
            @Parameter(description = "Track ID to update", example = "42")
            Long id,
            @Valid @RequestBody
            @Parameter(description = "Updated track data")
            TrackDto dto
    ) {
        TrackDto updated = trackService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete track",
            description = """
                    Deletes an existing track by its ID.
                    Typically used to clean up invalid or obsolete tracks.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Track deleted (no content)"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Track not found",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Unauthorized",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ErrorResponse.class)
                            )
                    )
            }
    )
    public ResponseEntity<Void> delete(
            @PathVariable
            @Parameter(description = "Track ID to delete", example = "42")
            Long id
    ) {
        trackService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
