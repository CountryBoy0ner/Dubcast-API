package com.Tsimur.Dubcast.radio;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.radio.events.NowPlayingChangedEvent;
import com.Tsimur.Dubcast.radio.events.ScheduleUpdatedEvent;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
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
    private final RadioProgrammingService radioProgrammingService;
    private final ApplicationEventPublisher eventPublisher;

    private ScheduleEntryDto cachedCurrent = null;

    private boolean autofillTriedForCurrentGap = false;

    @EventListener
    public void onScheduleUpdated(ScheduleUpdatedEvent event) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        // Если изменения только в будущем — текущий трек трогать не надо
        if (event.effectiveFrom().isAfter(now)) {
            log.debug("[RadioClock] Schedule updated in future ({}), ignore for now",
                    event.effectiveFrom());
            return;
        }

        log.debug("[RadioClock] Schedule updated from {} → recalc with now={}",
                event.effectiveFrom(), now);

        // При любом обновлении расписания считаем, что "дырка" могла исчезнуть
        autofillTriedForCurrentGap = false;
        recalcAndPublish(now);
    }

    @Scheduled(fixedRate = 1000)
    public void tick() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        recalcAndPublish(now);
    }

    private void recalcAndPublish(OffsetDateTime now) {
        // 1. Если в кэше есть трек и он ещё не закончился — вообще ничего не делаем, даже в БД не ходим.
        if (cachedCurrent != null && cachedCurrent.getEndTime().isAfter(now.toInstant())) {
            return;
        }

        // 2. Кеш либо пуст, либо трек закончился → пробуем найти текущий слот в БД.
        Optional<ScheduleEntryDto> opt = scheduleEntryService.getCurrent(now);

        // 3. Если ничего не нашли — пробуем автофилл (но не каждую секунду, а один раз на "дырку").
        if (opt.isEmpty()) {
            if (!autofillTriedForCurrentGap) {
                log.info("[RadioClock] No current track, trying autofill...");
                boolean filled = radioProgrammingService.ensureAutofillIfNeeded(now);
                autofillTriedForCurrentGap = true;

                if (filled) {
                    // Автофилл что-то добавил → перепробуем найти текущий слот.
                    opt = scheduleEntryService.getCurrent(now);
                }
            }
        } else {
            // Раз появился трек — сбрасываем флаг "уже пробовали автофилл"
            autofillTriedForCurrentGap = false;
        }

        ScheduleEntryDto newCurrent = opt.orElse(null);

        // 4. Если и раньше ничего не играло, и сейчас ничего — нечего публиковать.
        if (newCurrent == null && cachedCurrent == null) {
            return;
        }

        // 5. Если трек по сути тот же (по id) — событие тоже не шлём.
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
