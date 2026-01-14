package com.Tsimur.Dubcast.websocket;

import com.Tsimur.Dubcast.radio.events.TrackLikesChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TrackLikesWebSocketBroadcaster {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onTrackLikesChanged(TrackLikesChangedEvent e) {
    messagingTemplate.convertAndSend("/topic/track-likes", e);
  }
}
