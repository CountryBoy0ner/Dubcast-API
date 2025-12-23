package com.Tsimur.Dubcast.dto.response;

import java.time.Instant;
import lombok.*;

@Builder
@Data
public class NowPlayingResponse {
  private boolean playing;
  private String title;
  private String artworkUrl;
  private Instant startedAt;
  private int durationSeconds;
  private String trackUrl;

  private String playlistTitle;
}
