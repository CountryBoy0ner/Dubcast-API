package com.Tsimur.Dubcast.radio.autofill;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;

import java.time.OffsetDateTime;
import java.util.Optional;

public interface AutoFillStrategy {

    Optional<Long> chooseTrackIdForAutofill(OffsetDateTime now);
}
