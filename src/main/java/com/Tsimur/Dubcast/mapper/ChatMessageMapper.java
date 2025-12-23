package com.Tsimur.Dubcast.mapper;

import com.Tsimur.Dubcast.dto.ChatMessageDto;
import com.Tsimur.Dubcast.model.Message;
import com.Tsimur.Dubcast.model.User;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

  @Mapping(target = "username", expression = "java(resolveUsername(message.getSender()))")
  ChatMessageDto toDto(Message message);

  List<ChatMessageDto> toDtoList(List<Message> messages);

  default String resolveUsername(User sender) {
    if (sender == null) return "anonim";

    String username = sender.getUsername();
    if (username != null && !username.isBlank()) {
      return username.trim();
    }

    return "anonim";
  }
}
