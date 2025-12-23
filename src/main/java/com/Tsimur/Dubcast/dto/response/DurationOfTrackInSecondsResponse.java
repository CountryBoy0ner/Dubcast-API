package com.Tsimur.Dubcast.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DurationOfTrackInSecondsResponse {
  @NotBlank Integer durationSeconds;
}
