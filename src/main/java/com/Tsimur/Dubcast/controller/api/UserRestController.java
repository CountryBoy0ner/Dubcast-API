package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.UserDto;
import com.Tsimur.Dubcast.dto.request.ChangePasswordRequest;
import com.Tsimur.Dubcast.dto.request.CreateUserRequest;
import com.Tsimur.Dubcast.dto.request.UpdateUserRequest;
import com.Tsimur.Dubcast.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> create(@Valid @RequestBody CreateUserRequest request) {
        UserDto dto = UserDto.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        UserDto created = userService.create(dto, request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(userService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> update(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateUserRequest request) {
        UserDto dto = UserDto.builder()
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        UserDto updated = userService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id,
                                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(id, request.getPassword());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }






}
