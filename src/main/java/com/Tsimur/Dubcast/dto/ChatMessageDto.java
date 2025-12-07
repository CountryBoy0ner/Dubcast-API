package com.Tsimur.Dubcast.dto;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;


@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Data
@Builder
public class ChatMessageDto {
    private Long id;
    private String username;
    private String text;
    private OffsetDateTime createdAt;


}

