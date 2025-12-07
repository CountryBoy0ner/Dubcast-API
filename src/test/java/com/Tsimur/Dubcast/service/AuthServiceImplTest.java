package com.Tsimur.Dubcast.service;



import com.Tsimur.Dubcast.dto.request.LoginRequest;
import com.Tsimur.Dubcast.dto.request.RegisterRequest;
import com.Tsimur.Dubcast.dto.request.ValidateTokenRequest;
import com.Tsimur.Dubcast.dto.response.AuthResponse;
import com.Tsimur.Dubcast.dto.response.ValidateTokenResponse;
import com.Tsimur.Dubcast.exception.type.EmailAlreadyUsedException;
import com.Tsimur.Dubcast.model.Role;
import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.UserRepository;
import com.Tsimur.Dubcast.security.jwt.JwtService;
import com.Tsimur.Dubcast.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    // ---------- login ----------

    @Test
    void login_success() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("plainPassword");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "encodedPassword"))
                .thenReturn(true);
        when(jwtService.generateToken(user))
                .thenReturn("jwt-token-123");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getAccessToken());

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("plainPassword", "encodedPassword");
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_userNotFound_throws() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("whatever");

        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );
        assertEquals("User not found", ex.getMessage());

        verify(userRepository).findByEmail("notfound@example.com");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void login_invalidPassword_throws() {
        // given
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encodedPassword"))
                .thenReturn(false);

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> authService.login(request)
        );
        assertEquals("Invalid credentials", ex.getMessage());

        verify(userRepository).findByEmail("user@example.com");
        verify(passwordEncoder).matches("wrong", "encodedPassword");
        verifyNoInteractions(jwtService);
    }

    // ---------- register ----------

    @Test
    void register_success() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("plainPass");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPass")).thenReturn("hashedPass");
        when(jwtService.generateToken(any(User.class))).thenReturn("new-token");

        // мок save, чтобы вернуть того же юзера
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AuthResponse response = authService.register(request);

        // then
        assertNotNull(response);
        assertEquals("new-token", response.getAccessToken());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals("new@example.com", saved.getEmail());
        assertEquals("hashedPass", saved.getPassword());
        assertEquals(Role.ROLE_USER, saved.getRole());

        verify(userRepository).existsByEmail("new@example.com");
        verify(passwordEncoder).encode("plainPass");
        verify(jwtService).generateToken(saved);
    }

    @Test
    void register_emailAlreadyUsed_throws() {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setEmail("exists@example.com");
        request.setPassword("pass");

        when(userRepository.existsByEmail("exists@example.com"))
                .thenReturn(true);

        assertThrows(
                EmailAlreadyUsedException.class,
                () -> authService.register(request)
        );

        verify(userRepository).existsByEmail("exists@example.com");
        verifyNoInteractions(passwordEncoder, jwtService);
    }

    // ---------- validateToken ----------

    @Test
    void validateToken_validToken_returnsValidTrueWithEmailAndRole() {
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("jwt-token");

        when(jwtService.isExpired("jwt-token")).thenReturn(false);
        when(jwtService.extractEmail("jwt-token")).thenReturn("user@example.com");
        when(jwtService.extractRole("jwt-token")).thenReturn("ROLE_USER");

        ValidateTokenResponse response = authService.validateToken(request);

        assertTrue(response.isValid());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("ROLE_USER", response.getRole());

        verify(jwtService).isExpired("jwt-token");
        verify(jwtService).extractEmail("jwt-token");
        verify(jwtService).extractRole("jwt-token");
    }

    @Test
    void validateToken_expiredToken_returnsValidFalseAndNulls() {
        ValidateTokenRequest request = new ValidateTokenRequest();
        request.setToken("expired-token");

        when(jwtService.isExpired("expired-token")).thenReturn(true);

        ValidateTokenResponse response = authService.validateToken(request);

        assertFalse(response.isValid());
        assertNull(response.getEmail());
        assertNull(response.getRole());

        verify(jwtService).isExpired("expired-token");
        verify(jwtService, never()).extractEmail(anyString());
        verify(jwtService, never()).extractRole(anyString());
    }
}
