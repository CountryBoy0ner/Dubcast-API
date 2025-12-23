package com.Tsimur.Dubcast.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntryDto {

  private Long id;

  @NotNull private TrackDto track;

  @NotNull private Instant startTime;

  @NotNull private Instant endTime;

  private Long playlistId;
}
