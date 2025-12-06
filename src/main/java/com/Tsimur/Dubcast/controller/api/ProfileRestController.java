package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.request.UpdateBioRequest;
import com.Tsimur.Dubcast.dto.request.UpdateUsernameRequest;
import com.Tsimur.Dubcast.dto.response.UserProfileResponse;
import com.Tsimur.Dubcast.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileRestController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    @PutMapping("/bio")
    public ResponseEntity<Void> updateBio(@Valid @RequestBody UpdateBioRequest request) {
        userService.updateCurrentUserBio(request.getBio());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/username")
    public ResponseEntity<Void> updateUsername(
            @Valid @RequestBody UpdateUsernameRequest request
    ) {
        userService.updateCurrentUserUsername(request.getUsername());
        return ResponseEntity.noContent().build();
    }
}
