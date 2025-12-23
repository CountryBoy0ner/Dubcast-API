package com.Tsimur.Dubcast.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistTrackDto {

  private Long id;

  @NotNull @PositiveOrZero private Integer position;

  @NotNull private TrackDto track;
}
