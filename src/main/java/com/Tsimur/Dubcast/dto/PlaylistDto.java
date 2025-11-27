package com.Tsimur.Dubcast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistDto {

    private Long id;

    @NotBlank
    private String soundcloudUrl;

    @NotBlank
    private String title;

    @Positive
    private Integer totalTracks;
}
