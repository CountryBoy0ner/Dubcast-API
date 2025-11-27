package com.Tsimur.Dubcast.radio.events;

import java.time.OffsetDateTime;

public record ScheduleUpdatedEvent(OffsetDateTime effectiveFrom) {
}
