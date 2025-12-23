package com.Tsimur.Dubcast.radio.autofill;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface AutoFillStrategy {

  Optional<Long> chooseTrackIdForAutofill(OffsetDateTime now);
}
