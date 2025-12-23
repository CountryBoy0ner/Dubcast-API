package com.Tsimur.Dubcast.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrackDto {

  private Long id;

  @NotBlank private String soundcloudUrl;

  @NotBlank private String title;

  @NotNull @Positive private Integer durationSeconds;

  private String artworkUrl;
}
