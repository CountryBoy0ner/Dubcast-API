package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.request.UrlRequest;
import com.Tsimur.Dubcast.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping(ApiPaths.PROFILE)
@RequiredArgsConstructor
@Tag(
        name = "Playlists",
        description = "Endpoints for managing radio playlists and importing them from external sources."
)
public class PlaylistController {

    private final PlaylistService playlistService;

    @GetMapping
    @Operation(
            summary = "Get all playlists",
            description = "Returns a list of all playlists available in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of playlists successfully returned.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = PlaylistDto.class))
                    )
            )
    })
    public ResponseEntity<List<PlaylistDto>> getAll() {
        return ResponseEntity.ok(playlistService.getAll());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get playlist by ID",
            description = "Returns a single playlist by its identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Playlist found.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaylistDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Playlist with the given ID was not found.",
                    content = @Content
            )
    })
    public ResponseEntity<PlaylistDto> getById(
            @Parameter(description = "ID of the playlist", example = "1")
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(playlistService.getById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete playlist",
            description = "Deletes a playlist by its identifier. This operation is typically available only for admins."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Playlist successfully deleted.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Playlist with the given ID was not found.",
                    content = @Content
            )
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID of the playlist to delete", example = "1")
            @PathVariable Long id
    ) {
        playlistService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    @Operation(
            summary = "Import playlist from URL",
            description = """
                    Imports a playlist from an external URL (for example, SoundCloud playlist URL),
                    parses its metadata and stores it as a new playlist in the system.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Playlist successfully imported.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PlaylistDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or unsupported URL.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error during import.",
                    content = @Content
            )
    })
    public ResponseEntity<PlaylistDto> importPlaylist(
            @RequestBody @Valid UrlRequest request
    ) {
        PlaylistDto playlist = playlistService.importPlaylistFromUrl(request.getUrl());
        return ResponseEntity.ok(playlist);
    }
}
