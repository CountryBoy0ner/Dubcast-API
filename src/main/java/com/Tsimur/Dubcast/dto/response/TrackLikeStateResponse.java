package com.Tsimur.Dubcast.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrackLikeStateResponse {
  private Long trackId;
  private long likesCount;
  private boolean liked;
}
