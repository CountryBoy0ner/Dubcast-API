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

import java.time.Instant;
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

    // 1) кто-то обновил расписание (REST и т.п.)
    @EventListener
    public void onScheduleUpdated(ScheduleUpdatedEvent event) {
        log.debug("Schedule updated from {}", event.effectiveFrom());
        // переводим OffsetDateTime из события в Instant
        recalcAndPublish(event.effectiveFrom().toInstant());
    }

    // 2) тик раз в секунду
    @Scheduled(fixedRate = 1000)
    public void tick() {
        recalcAndPublish(Instant.now());
    }

    private void recalcAndPublish(Instant now) {
        // if есть кеш и трек ещё не закончился — не трогаем БД
        if (cachedCurrent != null && now.isBefore(cachedCurrent.getEndTime())) {
            return;
        }

        OffsetDateTime nowOffset = OffsetDateTime.ofInstant(now, ZoneOffset.UTC);
        Optional<ScheduleEntryDto> opt = scheduleEntryService.getCurrent(nowOffset);
        ScheduleEntryDto newCurrent = opt.orElse(null);

        if (newCurrent == null && cachedCurrent == null) {
            return;
        }

        if (sameTrack(cachedCurrent, newCurrent)) {
            return;
        }

        // обновляем кеш
        cachedCurrent = newCurrent;

        log.info("[RadioClock] Now playing changed: {}",
                newCurrent != null ? newCurrent.getTrack().getTitle() : "nothing");

        // публикуем доменное событие
        eventPublisher.publishEvent(new NowPlayingChangedEvent(newCurrent));
    }

    private boolean sameTrack(ScheduleEntryDto a, ScheduleEntryDto b) {
        Long aId = a != null ? a.getTrack().getId() : null;
        Long bId = b != null ? b.getTrack().getId() : null;
        return Objects.equals(aId, bId);
    }



}
