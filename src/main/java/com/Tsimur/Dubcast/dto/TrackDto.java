package com.Tsimur.Dubcast.dto;


import lombok.*;
import java.time.Instant;

import jakarta.validation.constraints.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackDto {

    private Long id;

    @NotBlank
    private String soundcloudUrl;

    @NotBlank
    private String embedCode;

    @NotBlank
    private String title;

    @NotNull
    @Positive
    private Integer durationSeconds;

    private String artworkUrl;

}
