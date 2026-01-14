package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.response.TrackLikeMeResponse;
import com.Tsimur.Dubcast.dto.response.TrackLikeStateResponse;
import com.Tsimur.Dubcast.model.TrackLike;
import com.Tsimur.Dubcast.radio.events.TrackLikesChangedEvent;
import com.Tsimur.Dubcast.repository.TrackLikeRepository;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.TrackLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackLikeServiceImpl implements TrackLikeService {

  private final TrackRepository trackRepository;
  private final TrackLikeRepository trackLikeRepository;
  private final CurrentUserService currentUserService;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public TrackLikeStateResponse like(Long trackId, String userEmail) {
    ensureTrackExists(trackId);

    var user = currentUserService.requireUserByEmail(userEmail);

    boolean alreadyLiked = trackLikeRepository.existsByUserIdAndTrackId(user.getId(), trackId);
    if (alreadyLiked) {
      long cnt = trackLikeRepository.countByTrackId(trackId);
      return new TrackLikeStateResponse(trackId, cnt, true);
    }

    try {
      trackLikeRepository.save(TrackLike.builder().userId(user.getId()).trackId(trackId).build());
    } catch (org.springframework.dao.DataIntegrityViolationException ignored) {
    }

    long cnt = trackLikeRepository.countByTrackId(trackId);
    eventPublisher.publishEvent(new TrackLikesChangedEvent(trackId, cnt));

    return new TrackLikeStateResponse(trackId, cnt, true);
  }

  @Override
  public TrackLikeStateResponse unlike(Long trackId, String userEmail) {
    ensureTrackExists(trackId);
    var user = currentUserService.requireUserByEmail(userEmail);

    int deleted = trackLikeRepository.deleteByUserIdAndTrackId(user.getId(), trackId);

    long cnt = trackLikeRepository.countByTrackId(trackId);
    if (deleted > 0) {
      eventPublisher.publishEvent(new TrackLikesChangedEvent(trackId, cnt));
    }
    return new TrackLikeStateResponse(trackId, cnt, false);
  }

  @Override
  @Transactional(readOnly = true)
  public TrackLikeMeResponse me(Long trackId, String userEmail) {
    ensureTrackExists(trackId);

    var user = currentUserService.requireUserByEmail(userEmail);

    boolean liked = trackLikeRepository.existsByUserIdAndTrackId(user.getId(), trackId);
    return new TrackLikeMeResponse(liked);
  }

  private void ensureTrackExists(Long trackId) {
    if (!trackRepository.existsById(trackId)) {
      throw new com.Tsimur.Dubcast.exception.type.NotFoundException("Track not found: " + trackId);
    }
  }
}
