package com.Tsimur.Dubcast.radio.events;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;

public record NowPlayingChangedEvent(ScheduleEntryDto current) {}
