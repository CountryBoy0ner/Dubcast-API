package com.Tsimur.Dubcast.controller;

import com.Tsimur.Dubcast.config.ApiPaths;
import com.Tsimur.Dubcast.dto.UserDto;
import com.Tsimur.Dubcast.dto.request.ChangePasswordRequest;
import com.Tsimur.Dubcast.dto.request.CreateUserRequest;
import com.Tsimur.Dubcast.dto.request.UpdateUserRequest;
import com.Tsimur.Dubcast.exception.ErrorResponse;
import com.Tsimur.Dubcast.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.USERS)
@Tag(
    name = "Users (Admin)",
    description =
        "Admin endpoints for managing users: create, read, update, delete and change password.")
@SecurityRequirement(name = "bearerAuth")
public class UserRestController {

  private final UserService userService;

  @PostMapping
  @Operation(
      summary = "Create a new user",
      description =
          """
                    Creates a new user with a given email, role and password.
                    This endpoint is intended for admin usage (e.g. back-office user management).
                    """,
      responses = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error in request body",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "409",
            description = "User with this email already exists",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized (no or invalid JWT)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (caller is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<UserDto> create(
      @Valid @RequestBody @Parameter(description = "User data and password to create")
          CreateUserRequest request) {
    UserDto dto = UserDto.builder().email(request.getEmail()).role(request.getRole()).build();

    UserDto created = userService.create(dto, request.getPassword());
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get user by ID",
      description = "Returns full user information for the given user ID.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User found",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
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
            description = "Forbidden (caller is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<UserDto> getById(
      @PathVariable
          @Parameter(description = "User ID", example = "550e8400-e29b-41d4-a716-446655440000")
          UUID id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @GetMapping
  @Operation(
      summary = "Get all users",
      description = "Returns the full list of users in the system.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of users",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden (caller is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<List<UserDto>> getAll() {
    return ResponseEntity.ok(userService.getAll());
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update user",
      description =
          """
                    Updates user's email and role.
                    Password is not changed here – use the dedicated change password endpoint.
                    """,
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "User updated",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error in request body",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
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
            description = "Forbidden (caller is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<UserDto> update(
      @PathVariable
          @Parameter(
              description = "User ID to update",
              example = "550e8400-e29b-41d4-a716-446655440000")
          UUID id,
      @Valid @RequestBody @Parameter(description = "Updated email and role")
          UpdateUserRequest request) {
    UserDto dto = UserDto.builder().email(request.getEmail()).role(request.getRole()).build();

    UserDto updated = userService.update(id, dto);
    return ResponseEntity.ok(updated);
  }

  @PostMapping("/{id}/password")
  @Operation(
      summary = "Change user password",
      description =
          """
                    Changes password for the specified user.
                    This is an admin-only operation and does not require the old password.
                    """,
      responses = {
        @ApiResponse(responseCode = "204", description = "Password changed successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Validation error in request body",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
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
            description = "Forbidden (caller is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> changePassword(
      @PathVariable
          @Parameter(
              description = "User ID whose password is changed",
              example = "550e8400-e29b-41d4-a716-446655440000")
          UUID id,
      @Valid @RequestBody @Parameter(description = "New password payload")
          ChangePasswordRequest request) {
    userService.changePassword(id, request.getPassword());
    return ResponseEntity.noContent().build();
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete user",
      description =
          """
                    Deletes a user by ID.
                    This operation is irreversible and should be used with care.
                    """,
      responses = {
        @ApiResponse(responseCode = "204", description = "User deleted (no content)"),
        @ApiResponse(
            responseCode = "404",
            description = "User not found",
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
            description = "Forbidden (caller is not an admin)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponse.class)))
      })
  public ResponseEntity<Void> delete(
      @PathVariable
          @Parameter(
              description = "User ID to delete",
              example = "550e8400-e29b-41d4-a716-446655440000")
          UUID id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
