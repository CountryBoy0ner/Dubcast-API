package com.Tsimur.Dubcast.controller;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.response.TrackLikeMeResponse;
import com.Tsimur.Dubcast.dto.response.TrackLikeStateResponse;
import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.service.TrackLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.LIKES)
@RequiredArgsConstructor
@Tag(
    name = "Track Likes",
    description = "Endpoints for liking/unliking tracks and checking current user's like status.")
@SecurityRequirement(name = "bearerAuth")
public class TrackLikeRestController {

  private final TrackLikeService trackLikeService;

  @PostMapping("/{trackId:\\d+}")
  @Operation(
      summary = "Like a track",
      description =
          """
                    Adds a like from the current authenticated user to the specified track.
                    If the track is already liked by the user, the operation is idempotent.
                    Returns the updated likes counter and current user's like state.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Like state returned (liked=true)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrackLikeStateResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (no or invalid token)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (user has no access)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Track not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<TrackLikeStateResponse> like(
      @PathVariable @Parameter(description = "Track ID", example = "1") Long trackId,
      Authentication auth) {

    return ResponseEntity.ok(trackLikeService.like(trackId, auth.getName()));
  }

  @DeleteMapping("/{trackId:\\d+}")
  @Operation(
      summary = "Unlike a track",
      description =
          """
                    Removes the like from the current authenticated user for the specified track.
                    If the track is not liked, the operation is idempotent.
                    Returns the updated likes counter and current user's like state.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Like state returned (liked=false)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrackLikeStateResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (no or invalid token)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (user has no access)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Track not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<TrackLikeStateResponse> unlike(
      @PathVariable @Parameter(description = "Track ID", example = "1") Long trackId,
      Authentication auth) {

    return ResponseEntity.ok(trackLikeService.unlike(trackId, auth.getName()));
  }

  @GetMapping("/{trackId:\\d+}/me")
  @Operation(
      summary = "Get current user's like status for a track",
      description =
          """
                    Returns whether the current authenticated user has liked the specified track.

                    Use this endpoint to render UI state (e.g., heart icon filled/unfilled).
                    The response is user-specific and requires authentication.
                    """,
      parameters = {
        @Parameter(
            name = "trackId",
            description = "Track ID to check like status for",
            example = "1",
            required = true)
      },
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Like status for current user returned",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TrackLikeMeResponse.class),
                    examples = {
                      @ExampleObject(
                          name = "Liked",
                          summary = "User has liked the track",
                          value =
                              """
                                                            { "trackId": 1, "liked": true }
                                                            """),
                      @ExampleObject(
                          name = "Not liked",
                          summary = "User has not liked the track",
                          value =
                              """
                                                            { "trackId": 1, "liked": false }
                                                            """)
                    })),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (no or invalid token)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class),
                    examples =
                        @ExampleObject(
                            value =
                                """
                                                    { "message": "Unauthorized", "status": 401, "path": "/api/likes/1/me" }
                                                    """))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (user has no access)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Track not found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<TrackLikeMeResponse> me(@PathVariable Long trackId, Authentication auth) {

    return ResponseEntity.ok(trackLikeService.me(trackId, auth.getName()));
  }
}
