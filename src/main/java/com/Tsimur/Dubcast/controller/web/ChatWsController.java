package com.Tsimur.Dubcast.controller.api;

import com.Tsimur.Dubcast.dto.ChatMessageDto;
import com.Tsimur.Dubcast.dto.request.ChatMessageIncomingDto;
import com.Tsimur.Dubcast.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final MessageService messageService;


}
