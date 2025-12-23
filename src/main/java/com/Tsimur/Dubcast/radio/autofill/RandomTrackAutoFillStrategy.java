package com.Tsimur.Dubcast.radio.autofill;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.service.TrackService;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RandomTrackAutoFillStrategy implements AutoFillStrategy {

  private final TrackService trackService;

  @Override
  public Optional<Long> chooseTrackIdForAutofill(OffsetDateTime now) {
    return trackService
        .getRandomTrack()
        .filter(t -> t.getDurationSeconds() != null && t.getDurationSeconds() > 0)
        .map(TrackDto::getId);
  }
}
