package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.request.UpdateBioRequest;
import com.Tsimur.Dubcast.dto.request.UpdateUsernameRequest;
import com.Tsimur.Dubcast.dto.response.UserProfileResponse;
import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(ApiPaths.PROFILE)
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Endpoints for managing the current user's profile.")
@SecurityRequirement(name = "bearerAuth")
public class ProfileRestController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(
      summary = "Get current user's profile",
      description =
          "Returns email, username, bio and other data of the currently authenticated user.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserProfileResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<UserProfileResponse> getMe() {
    return ResponseEntity.ok(userService.getCurrentUserProfile());
  }

  @PutMapping("/bio")
  @Operation(
      summary = "Update bio",
      description = "Updates the bio field of the current user.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Bio updated"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> updateBio(@Valid @RequestBody UpdateBioRequest request) {
    userService.updateCurrentUserBio(request.getBio());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/username")
  @Operation(
      summary = "Update username",
      description = "Updates the current user's username. Must be unique.",
      responses = {
        @ApiResponse(responseCode = "204", description = "Username updated"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "Username already in use",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> updateUsername(@Valid @RequestBody UpdateUsernameRequest request) {
    userService.updateCurrentUserUsername(request.getUsername());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/public/{username}")
  @Operation(
      summary = "Get public profile by username",
      description =
          "Returns public user profile info (username and bio) by username. Does not return email.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Profile found.",
        content =
            @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User with the given username was not found.",
        content = @Content)
  })
  public ResponseEntity<UserProfileResponse> getPublicProfileByUsername(
      @Parameter(description = "Username of the user", example = "tsimur") @PathVariable
          String username) {
    return ResponseEntity.ok(userService.getCurrentUserProfileByUsername(username));
  }
}
