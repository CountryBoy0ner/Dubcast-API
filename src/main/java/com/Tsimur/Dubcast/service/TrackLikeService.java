package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.response.TrackLikeMeResponse;
import com.Tsimur.Dubcast.dto.response.TrackLikeStateResponse;

public interface TrackLikeService {
  TrackLikeStateResponse like(Long trackId, String userEmail);

  TrackLikeStateResponse unlike(Long trackId, String userEmail);

  TrackLikeMeResponse me(Long trackId, String userEmail);
}
