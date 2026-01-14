package com.Tsimur.Dubcast.dto.response;

import lombok.Data;

@Data
public class TrackLikesUpdatedResponse {
  private Long trackId;
  private Long likesCount;
}
