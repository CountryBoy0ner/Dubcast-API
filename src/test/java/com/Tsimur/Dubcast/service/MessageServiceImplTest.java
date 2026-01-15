package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Tsimur.Dubcast.dto.ChatMessageDto;
import com.Tsimur.Dubcast.mapper.ChatMessageMapper;
import com.Tsimur.Dubcast.model.Message;
import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.MessageRepository;
import com.Tsimur.Dubcast.repository.UserRepository;
import com.Tsimur.Dubcast.service.impl.MessageServiceImpl;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

  @Mock private MessageRepository messageRepository;

  @Mock private UserRepository userRepository;

  @Mock private ChatMessageMapper chatMessageMapper;

  @InjectMocks private MessageServiceImpl messageService;

  // ========= saveMessage =========

  @Test
  void saveMessage_success() {
    // given
    String email = "user@example.com";
    String text = "   Hello world   ";

    User user = new User();
    user.setEmail(email);

    Message savedMessage = new Message();
    savedMessage.setId(1L);
    savedMessage.setSender(user);
    savedMessage.setText("Hello world");

    ChatMessageDto dto =
        ChatMessageDto.builder()
            .id(1L)
            .username("user")
            .text("Hello world")
            .createdAt(OffsetDateTime.now())
            .build();

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(messageRepository.save(any(Message.class))).thenReturn(savedMessage);
    when(chatMessageMapper.toDto(savedMessage)).thenReturn(dto);

    // when
    ChatMessageDto result = messageService.saveMessage(text, email);

    // then
    assertNotNull(result);
    assertEquals(1L, result.getId());
    assertEquals("Hello world", result.getText());

    // capture message passed to repository to check trimming & fields
    ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository).save(messageCaptor.capture());
    Message toSave = messageCaptor.getValue();

    assertEquals(user, toSave.getSender());
    assertEquals("Hello world", toSave.getText());
    assertNotNull(toSave.getCreatedAt());

    verify(userRepository).findByEmail(email);
    verify(chatMessageMapper).toDto(savedMessage);
  }

  @Test
  void saveMessage_nullOrBlank_throwsIllegalArgumentException() {
    // null
    assertThrows(
        IllegalArgumentException.class, () -> messageService.saveMessage(null, "user@example.com"));

    // blank
    assertThrows(
        IllegalArgumentException.class,
        () -> messageService.saveMessage("   ", "user@example.com"));

    verifyNoInteractions(userRepository, messageRepository, chatMessageMapper);
  }

  @Test
  void saveMessage_userNotFound_throwsIllegalStateException() {
    // given
    when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

    // when / then
    IllegalStateException ex =
        assertThrows(
            IllegalStateException.class,
            () -> messageService.saveMessage("Hello", "missing@example.com"));

    assertTrue(ex.getMessage().contains("Current user not found"));
    verify(userRepository).findByEmail("missing@example.com");
    verifyNoInteractions(messageRepository, chatMessageMapper);
  }

  // ========= getLastMessages =========

  @Test
  void getLastMessages_usesDefaultSizeWhenLimitInvalidAndReversesOrder() {
    // limit <= 0 -> size = 50
    int limit = 0;
    int expectedSize = 50;

    // repository returns messages sorted DESC by createdAt
    Message m3 = new Message();
    m3.setId(3L);
    Message m2 = new Message();
    m2.setId(2L);
    Message m1 = new Message();
    m1.setId(1L);

    List<Message> repoList = List.of(m3, m2, m1);
    Page<Message> page = new PageImpl<>(repoList);

    when(messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, expectedSize)))
        .thenReturn(page);

    // mapper returns dtos with the same ids, but we also want to check order
    when(chatMessageMapper.toDtoList(anyList()))
        .thenAnswer(
            invocation -> {
              List<Message> msgs = invocation.getArgument(0);
              List<ChatMessageDto> dtos = new ArrayList<>();
              for (Message m : msgs) {
                dtos.add(
                    ChatMessageDto.builder()
                        .id(m.getId())
                        .username("u" + m.getId())
                        .text("t" + m.getId())
                        .createdAt(OffsetDateTime.now())
                        .build());
              }
              return dtos;
            });

    // when
    List<ChatMessageDto> result = messageService.getLastMessages(limit);

    // then
    assertEquals(3, result.size());
    // order should be reversed (oldest first: 1,2,3)
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
    assertEquals(3L, result.get(2).getId());

    ArgumentCaptor<List<Message>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(chatMessageMapper).toDtoList(listCaptor.capture());
    List<Message> passedToMapper = listCaptor.getValue();

    assertEquals(3, passedToMapper.size());
    assertEquals(1L, passedToMapper.get(0).getId());
    assertEquals(2L, passedToMapper.get(1).getId());
    assertEquals(3L, passedToMapper.get(2).getId());
  }

  @Test
  void getLastMessages_limitTooBig_clampedTo200() {
    int limit = 1000; // > 200 -> use 50

    Message m = new Message();
    m.setId(10L);
    List<Message> list = List.of(m);
    Page<Message> page = new PageImpl<>(list);

    when(messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50))).thenReturn(page);
    when(chatMessageMapper.toDtoList(anyList()))
        .thenReturn(
            List.of(
                ChatMessageDto.builder()
                    .id(10L)
                    .username("u10")
                    .text("t10")
                    .createdAt(OffsetDateTime.now())
                    .build()));

    List<ChatMessageDto> result = messageService.getLastMessages(limit);

    assertEquals(1, result.size());
    assertEquals(10L, result.get(0).getId());
    verify(messageRepository).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50));
  }

  // ========= getMessagesPage =========

  @Test
  void getMessagesPage_negativePageAndTooBigSize_areClampedAndOrderReversed() {
    int pageIndex = -5; // -> safePage = 0
    int size = 1000; // -> safeSize = 50

    Message m3 = new Message();
    m3.setId(3L);
    Message m2 = new Message();
    m2.setId(2L);
    Message m1 = new Message();
    m1.setId(1L);

    List<Message> repoList = List.of(m3, m2, m1);
    Page<Message> page = new PageImpl<>(repoList);

    when(messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50))).thenReturn(page);

    when(chatMessageMapper.toDtoList(anyList()))
        .thenAnswer(
            invocation -> {
              List<Message> msgs = invocation.getArgument(0);
              List<ChatMessageDto> dtos = new ArrayList<>();
              for (Message m : msgs) {
                dtos.add(
                    ChatMessageDto.builder()
                        .id(m.getId())
                        .username("u" + m.getId())
                        .text("t" + m.getId())
                        .createdAt(OffsetDateTime.now())
                        .build());
              }
              return dtos;
            });

    // when
    List<ChatMessageDto> result = messageService.getMessagesPage(pageIndex, size);

    // then
    assertEquals(3, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
    assertEquals(3L, result.get(2).getId());

    verify(messageRepository).findAllByOrderByCreatedAtDesc(PageRequest.of(0, 50));
  }

  @Test
  void getMessagesPage_normalParams() {
    int pageIndex = 2;
    int size = 20;

    Message m = new Message();
    m.setId(42L);
    List<Message> repoList = List.of(m);
    Page<Message> page = new PageImpl<>(repoList);

    when(messageRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(pageIndex, size)))
        .thenReturn(page);

    when(chatMessageMapper.toDtoList(anyList()))
        .thenReturn(
            List.of(
                ChatMessageDto.builder()
                    .id(42L)
                    .username("u42")
                    .text("t42")
                    .createdAt(OffsetDateTime.now())
                    .build()));

    List<ChatMessageDto> result = messageService.getMessagesPage(pageIndex, size);

    assertEquals(1, result.size());
    assertEquals(42L, result.get(0).getId());

    verify(messageRepository).findAllByOrderByCreatedAtDesc(PageRequest.of(pageIndex, size));
    verify(chatMessageMapper).toDtoList(anyList());
  }
}
