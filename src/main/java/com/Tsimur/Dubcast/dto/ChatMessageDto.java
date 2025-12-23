package com.Tsimur.Dubcast.dto;

import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Data
@Builder
public class ChatMessageDto {
  private Long id;
  private String username;
  private String text;
  private OffsetDateTime createdAt;
}
