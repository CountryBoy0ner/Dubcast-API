package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.ChatMessageDto;

import java.util.List;

public interface MessageService {
    ChatMessageDto saveMessage(String text, String userEmail);

    List<ChatMessageDto> getLastMessages(int limit);

    List<ChatMessageDto> getMessagesPage(int page, int size);

}
