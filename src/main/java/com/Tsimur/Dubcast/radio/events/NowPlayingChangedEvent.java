package com.Tsimur.Dubcast.radio.events;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import org.springframework.lang.Nullable;

public record NowPlayingChangedEvent(@Nullable ScheduleEntryDto current) {
}
