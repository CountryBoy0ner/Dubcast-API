package com.Tsimur.Dubcast.radio;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.radio.events.NowPlayingChangedEvent;
import com.Tsimur.Dubcast.radio.events.ScheduleUpdatedEvent;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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

    if (event.effectiveFrom().isAfter(now)) {
      log.debug(
          "[RadioClock] Schedule updated in future ({}), ignore for now", event.effectiveFrom());
      return;
    }

    log.debug(
        "[RadioClock] Schedule updated from {} → recalc with now={}", event.effectiveFrom(), now);

    autofillTriedForCurrentGap = false;
    recalcAndPublish(now);
  }

  @Scheduled(fixedRate = 1000)
  public void tick() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    recalcAndPublish(now);
  }

  private void recalcAndPublish(OffsetDateTime now) {
    if (cachedCurrent != null && cachedCurrent.getEndTime().isAfter(now.toInstant())) {
      return;
    }

    Optional<ScheduleEntryDto> opt = scheduleEntryService.getCurrent(now);

    if (opt.isEmpty()) {
      if (!autofillTriedForCurrentGap) {
        log.info("[RadioClock] No current track, trying autofill...");
        boolean filled = radioProgrammingService.ensureAutofillIfNeeded(now);
        autofillTriedForCurrentGap = true;

        if (filled) {
          opt = scheduleEntryService.getCurrent(now);
        }
      }
    } else {
      autofillTriedForCurrentGap = false;
    }

    ScheduleEntryDto newCurrent = opt.orElse(null);

    if (newCurrent == null && cachedCurrent == null) {
      return;
    }
    if (sameTrack(cachedCurrent, newCurrent)) {
      return;
    }

    cachedCurrent = newCurrent;

    String title =
        (newCurrent != null && newCurrent.getTrack() != null)
            ? newCurrent.getTrack().getTitle()
            : "nothing";
    log.info("[RadioClock] Now playing changed: {}", title);

    eventPublisher.publishEvent(new NowPlayingChangedEvent(newCurrent));
  }

  private boolean sameTrack(ScheduleEntryDto a, ScheduleEntryDto b) {
    Long aId = (a != null && a.getTrack() != null) ? a.getTrack().getId() : null;
    Long bId = (b != null && b.getTrack() != null) ? b.getTrack().getId() : null;

    return Objects.equals(aId, bId);
  }
}
