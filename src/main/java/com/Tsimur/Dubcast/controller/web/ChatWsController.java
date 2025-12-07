package com.Tsimur.Dubcast.controller.web;

import com.Tsimur.Dubcast.dto.ChatMessageDto;
import com.Tsimur.Dubcast.dto.request.ChatMessageIncomingDto;
import com.Tsimur.Dubcast.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final MessageService messageService;

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public ChatMessageDto handleSend(
            @Valid @Payload ChatMessageIncomingDto incoming,
            Principal principal
    ) {
        if (principal == null) {
            throw new IllegalStateException("User must be authenticated to send messages");
        }

        return messageService.saveMessage(incoming.getText(), principal.getName());
    }
}
