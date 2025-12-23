package com.Tsimur.Dubcast.dto;

import java.time.OffsetDateTime;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminScheduleSlotDto {

  private Long id;

  private String trackTitle;
  private String trackArtworkUrl;
  private String trackScUrl;

  private Long playlistId;
  private String playlistName;

  private OffsetDateTime startTime;
  private OffsetDateTime endTime;
}
