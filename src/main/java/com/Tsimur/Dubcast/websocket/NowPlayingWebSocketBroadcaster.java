package com.Tsimur.Dubcast.websocket;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.response.NowPlayingMessageResponse;
import com.Tsimur.Dubcast.radio.events.NowPlayingChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;


@Deprecated // TODO: delete or change after implementing Playlists
@Slf4j
@Component
@RequiredArgsConstructor
public class NowPlayingWebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleNowPlayingChanged(NowPlayingChangedEvent event) {
        ScheduleEntryDto current = event.current();

        NowPlayingMessageResponse payload = NowPlayingMessageResponse.from(current);

        if (payload.isPlaying()) {
            log.info("[WS] Broadcasting now playing: {}", payload.getTitle());
        } else {
            log.info("[WS] Broadcasting now playing: nothing");
        }

        messagingTemplate.convertAndSend("/topic/now-playing", payload);
    }
}
