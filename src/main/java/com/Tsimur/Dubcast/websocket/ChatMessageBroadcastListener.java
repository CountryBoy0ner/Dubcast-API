package com.Tsimur.Dubcast.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ChatMessageBroadcastListener {

  private final SimpMessagingTemplate messagingTemplate;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onChatMessageCreated(ChatMessageCreatedEvent event) {
    messagingTemplate.convertAndSend("/topic/chat", event.message());
  }
}
