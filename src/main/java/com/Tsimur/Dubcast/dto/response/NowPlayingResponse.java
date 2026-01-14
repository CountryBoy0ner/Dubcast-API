package com.Tsimur.Dubcast.dto.response;

import java.time.Instant;
import lombok.*;

@Builder
@Data
public class NowPlayingResponse {
  private boolean playing;
  private Long trackId;
  private String title;
  private String artworkUrl;
  private Instant startedAt;
  private int durationSeconds;
  private String trackUrl;
  private String playlistTitle;
  private long likesCount;
}
