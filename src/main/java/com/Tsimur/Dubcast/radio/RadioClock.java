package com.Tsimur.Dubcast.radio;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.radio.events.NowPlayingChangedEvent;
import com.Tsimur.Dubcast.radio.events.ScheduleUpdatedEvent;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RadioClock {

    private final ScheduleEntryService scheduleEntryService;
    private final ApplicationEventPublisher eventPublisher;

    private ScheduleEntryDto cachedCurrent = null;

    @EventListener
    public void onScheduleUpdated(ScheduleUpdatedEvent event) {
        log.debug("[RadioClock] Schedule updated from {}", event.effectiveFrom());
        recalcAndPublish(event.effectiveFrom());
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC); // важно: те же зоны, что в расписании
        recalcAndPublish(now);
    }

    private void recalcAndPublish(OffsetDateTime now) {
        if (cachedCurrent != null && cachedCurrent.getEndTime().isAfter(now.toInstant())) {
            return;
        }

        Optional<ScheduleEntryDto> opt = scheduleEntryService.getCurrent(now);
        ScheduleEntryDto newCurrent = opt.orElse(null);

        if (newCurrent == null && cachedCurrent == null) {
            return;
        }

        if (sameTrack(cachedCurrent, newCurrent)) {
            return;
        }

        cachedCurrent = newCurrent;

        log.info("[RadioClock] Now playing changed: {}",
                newCurrent != null ? newCurrent.getTrack().getTitle() : "nothing");

        eventPublisher.publishEvent(new NowPlayingChangedEvent(newCurrent));
    }

    private boolean sameTrack(ScheduleEntryDto a, ScheduleEntryDto b) {
        Long aId = a != null ? a.getTrack().getId() : null;
        Long bId = b != null ? b.getTrack().getId() : null;
        return Objects.equals(aId, bId);
    }
}
