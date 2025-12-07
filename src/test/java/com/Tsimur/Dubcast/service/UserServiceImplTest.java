package com.Tsimur.Dubcast.service;


import com.Tsimur.Dubcast.dto.UserDto;
import com.Tsimur.Dubcast.dto.response.UserProfileResponse;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.UserMapper;
import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.UserRepository;
import com.Tsimur.Dubcast.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);
    }

    // ------------------------------------------------------------------------
    // create
    // ------------------------------------------------------------------------
    @Test
    void create_shouldCreateUser_whenEmailNotExists() {
        UserDto requestDto = UserDto.builder()
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        User entityToSave = new User();
        User savedEntity = new User();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail("test@example.com");

        UserDto responseDto = UserDto.builder()
                .id(savedEntity.getId())
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userMapper.toEntity(requestDto)).thenReturn(entityToSave);
        when(passwordEncoder.encode("raw-pass")).thenReturn("encoded-pass");
        when(userRepository.save(entityToSave)).thenReturn(savedEntity);
        when(userMapper.toDto(savedEntity)).thenReturn(responseDto);

        UserDto result = userService.create(requestDto, "raw-pass");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("ROLE_USER", result.getRole());

        verify(userRepository).existsByEmail("test@example.com");
        verify(userMapper).toEntity(requestDto);
        verify(passwordEncoder).encode("raw-pass");
        verify(userRepository).save(entityToSave);
        verify(userMapper).toDto(savedEntity);
    }

    @Test
    void create_shouldThrow_whenEmailAlreadyExists() {
        UserDto dto = UserDto.builder()
                .email("test@example.com")
                .role("ROLE_USER")
                .build();

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.create(dto, "pass"));

        verify(userRepository).existsByEmail("test@example.com");
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------------
    // getById
    // ------------------------------------------------------------------------
    @Test
    void getById_shouldReturnUser_whenFound() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);

        UserDto dto = UserDto.builder()
                .id(id)
                .email("u@example.com")
                .role("ROLE_USER")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.getById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(userRepository).findById(id);
        verify(userMapper).toDto(user);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> userService.getById(id));

        verify(userRepository).findById(id);
    }

    // ------------------------------------------------------------------------
    // getAll
    // ------------------------------------------------------------------------
    @Test
    void getAll_shouldReturnList() {
        User u1 = new User();
        User u2 = new User();
        List<User> entities = List.of(u1, u2);

        UserDto d1 = UserDto.builder().email("u1@example.com").build();
        UserDto d2 = UserDto.builder().email("u2@example.com").build();
        List<UserDto> dtos = List.of(d1, d2);

        when(userRepository.findAll()).thenReturn(entities);
        when(userMapper.toDtoList(entities)).thenReturn(dtos);

        List<UserDto> result = userService.getAll();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(userMapper).toDtoList(entities);
    }

    // ------------------------------------------------------------------------
    // update
    // ------------------------------------------------------------------------
    @Test
    void update_shouldUpdate_whenEmailNotChangedOrFree() {
        UUID id = UUID.randomUUID();

        User existing = new User();
        existing.setId(id);
        existing.setEmail("old@example.com");

        UserDto dto = UserDto.builder()
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        User saved = new User();
        saved.setId(id);
        saved.setEmail("new@example.com");

        UserDto resultDto = UserDto.builder()
                .id(id)
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        // void method
        doNothing().when(userMapper).updateEntityFromDto(dto, existing);
        when(userRepository.save(existing)).thenReturn(saved);
        when(userMapper.toDto(saved)).thenReturn(resultDto);

        UserDto result = userService.update(id, dto);

        assertNotNull(result);
        assertEquals("new@example.com", result.getEmail());
        assertEquals("ROLE_ADMIN", result.getRole());

        verify(userRepository).findById(id);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userMapper).updateEntityFromDto(dto, existing);
        verify(userRepository).save(existing);
        verify(userMapper).toDto(saved);
    }

    @Test
    void update_shouldThrow_whenNewEmailAlreadyExists() {
        UUID id = UUID.randomUUID();
        User existing = new User();
        existing.setId(id);
        existing.setEmail("old@example.com");

        UserDto dto = UserDto.builder()
                .email("new@example.com")
                .role("ROLE_ADMIN")
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.update(id, dto));

        verify(userRepository).findById(id);
        verify(userRepository).existsByEmail("new@example.com");
        verify(userMapper, never()).updateEntityFromDto(any(), any());
        verify(userRepository, never()).save(any());
    }

    // ------------------------------------------------------------------------
    // delete
    // ------------------------------------------------------------------------
    @Test
    void delete_shouldDelete_whenExists() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(true);

        userService.delete(id);

        verify(userRepository).existsById(id);
        verify(userRepository).deleteById(id);
    }

    @Test
    void delete_shouldThrow_whenNotExists() {
        UUID id = UUID.randomUUID();
        when(userRepository.existsById(id)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> userService.delete(id));

        verify(userRepository).existsById(id);
        verify(userRepository, never()).deleteById(any());
    }

    // ------------------------------------------------------------------------
    // changePassword
    // ------------------------------------------------------------------------
    @Test
    void changePassword_shouldEncodeAndSave() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setPassword("old");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("new-pass")).thenReturn("encoded-new");
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(id, "new-pass");

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).findById(id);
        verify(passwordEncoder).encode("new-pass");
        verify(userRepository).save(user);
    }

    // ------------------------------------------------------------------------
    // current user (SecurityContextHolder)
    // ------------------------------------------------------------------------
    @Test
    void getCurrentUserProfile_shouldReturnProfile() {
        String email = "me@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("myname");
        user.setBio("my bio");

        // static mock SecurityContextHolder
        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);

            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

            UserProfileResponse resp = userService.getCurrentUserProfile();

            assertNotNull(resp);
            assertEquals("myname", resp.getUsername());
            assertEquals("my bio", resp.getBio());

            verify(userRepository).findByEmail(email);
        }
    }

    @Test
    void updateCurrentUserBio_shouldUpdateAndSave() {
        String email = "me@example.com";
        User user = new User();
        user.setEmail(email);
        user.setBio("old");

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);

            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            userService.updateCurrentUserBio("new bio");

            assertEquals("new bio", user.getBio());
            verify(userRepository).save(user);
        }
    }

    @Test
    void updateCurrentUserUsername_shouldThrow_whenUsernameAlreadyTaken() {
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateCurrentUserUsername("taken"));

        verify(userRepository).existsByUsername("taken");
        // getCurrentUserOrThrow не вызывается из-за раннего выхода
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    void updateCurrentUserUsername_shouldUpdateAndSave_whenFree() {
        String email = "me@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("old");

        when(userRepository.existsByUsername("newname")).thenReturn(false);

        try (MockedStatic<SecurityContextHolder> mocked = mockStatic(SecurityContextHolder.class)) {
            SecurityContext context = mock(SecurityContext.class);
            Authentication auth = mock(Authentication.class);

            mocked.when(SecurityContextHolder::getContext).thenReturn(context);
            when(context.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn(email);
            when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);

            userService.updateCurrentUserUsername("newname");

            assertEquals("newname", user.getUsername());
            verify(userRepository).save(user);
        }
    }
}
