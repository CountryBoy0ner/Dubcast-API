package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.ChatMessageDto;
import com.Tsimur.Dubcast.mapper.ChatMessageMapper;
import com.Tsimur.Dubcast.model.Message;
import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.MessageRepository;
import com.Tsimur.Dubcast.repository.UserRepository;
import com.Tsimur.Dubcast.service.MessageService;
import com.Tsimur.Dubcast.websocket.ChatMessageCreatedEvent;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final ChatMessageMapper chatMessageMapper;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  public ChatMessageDto saveMessage(String text, String userEmail) {
    String trimmed = text == null ? "" : text.trim();
    if (trimmed.isEmpty()) {
      throw new IllegalArgumentException("Message text must not be empty");
    }

    User sender =
        userRepository
            .findByEmail(userEmail)
            .orElseThrow(() -> new IllegalStateException("Current user not found: " + userEmail));

    Message message = new Message();
    message.setSender(sender);
    message.setText(trimmed);
    message.setCreatedAt(OffsetDateTime.now());

    Message saved = messageRepository.save(message);
    return chatMessageMapper.toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageDto> getLastMessages(int limit) {
    int size = (limit <= 0 || limit > 200) ? 50 : limit;

    var page = messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, size));

    // ДЕЛАЕМ МУТИРУЕМУЮ КОПИЮ
    List<Message> messages = new ArrayList<>(page.getContent());
    Collections.reverse(messages); // теперь работает нормально

    return chatMessageMapper.toDtoList(messages);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMessageDto> getMessagesPage(int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = (size <= 0 || size > 200) ? 50 : size;

    var pageable = PageRequest.of(safePage, safeSize);
    var pageResult = messageRepository.findAllByOrderByCreatedAtDesc(pageable);

    // Тоже самое — копия + reverse
    List<Message> messages = new ArrayList<>(pageResult.getContent());
    Collections.reverse(messages);

    return chatMessageMapper.toDtoList(messages);
  }

  @Override
  public ChatMessageDto saveMessageAndPublish(String text, String userEmail) {
    ChatMessageDto saved = saveMessage(text, userEmail);
    eventPublisher.publishEvent(new ChatMessageCreatedEvent(saved));
    return saved;
  }
}
