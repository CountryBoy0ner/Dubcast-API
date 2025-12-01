package com.Tsimur.Dubcast.websocket;

import com.Tsimur.Dubcast.dto.response.NowPlayingResponse;
import com.Tsimur.Dubcast.radio.NowPlayingResponseFactory;
import com.Tsimur.Dubcast.radio.events.NowPlayingChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NowPlayingWebSocketBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final NowPlayingResponseFactory nowPlayingResponseFactory;

    @EventListener
    public void onNowPlayingChanged(NowPlayingChangedEvent event) {
        NowPlayingResponse dto = nowPlayingResponseFactory.fromScheduleEntry(event.current());
        log.info("[WS] Broadcasting now playing: {}", dto.getTitle());
        messagingTemplate.convertAndSend("/topic/now-playing", dto);
    }
}
