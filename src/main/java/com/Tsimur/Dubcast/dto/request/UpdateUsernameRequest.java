package com.Tsimur.Dubcast.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUsernameRequest {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(
            regexp = "^[A-Za-z0-9_.]+$",
            message = "Username can contain letters, digits, underscore and dot"
    )
    private String username;
}
