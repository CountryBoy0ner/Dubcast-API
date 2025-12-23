package com.Tsimur.Dubcast.websocket;

import com.Tsimur.Dubcast.dto.ChatMessageDto;

public record ChatMessageCreatedEvent(ChatMessageDto message) {}
