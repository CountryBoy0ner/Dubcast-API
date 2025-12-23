package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;

import com.Tsimur.Dubcast.service.impl.SoundcloudClientIdProviderImpl;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SoundcloudClientIdProviderImplTest {

  private SoundcloudClientIdProviderImpl provider;

  @BeforeEach
  void setUp() {
    provider = new SoundcloudClientIdProviderImpl();
  }

  @Test
  void getClientId_shouldReturnCachedValue_whenAlreadyResolved() throws Exception {
    // Arrange: write value into cachedClientId via reflection
    Field field = SoundcloudClientIdProviderImpl.class.getDeclaredField("cachedClientId");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    AtomicReference<String> ref = (AtomicReference<String>) field.get(provider);
    ref.set("cached-123");

    // Act
    String result = provider.getClientId();

    // Assert
    assertEquals("cached-123", result);
  }

  @Test
  void invalidate_shouldClearCachedClientId() throws Exception {
    // Arrange: put some value into the cache
    Field field = SoundcloudClientIdProviderImpl.class.getDeclaredField("cachedClientId");
    field.setAccessible(true);
    @SuppressWarnings("unchecked")
    AtomicReference<String> ref = (AtomicReference<String>) field.get(provider);
    ref.set("cached-xyz");

    // Act
    provider.invalidate();

    // Assert
    assertNull(ref.get());
  }

  @Test
  void extractClientIdFromUrl_shouldReturnClientId_whenPresentInQuery() throws Exception {
    // Private method call via reflection
    Method method =
        SoundcloudClientIdProviderImpl.class.getDeclaredMethod(
            "extractClientIdFromUrl", String.class);
    method.setAccessible(true);

    String clientId = "abc123";
    String encodedClientId = URLEncoder.encode(clientId, StandardCharsets.UTF_8);
    String url =
        "https://api-v2.soundcloud.com/tracks/1?client_id=" + encodedClientId + "&format=json";

    // Act
    String result = (String) method.invoke(provider, url);

    // Assert
    assertEquals(clientId, result);
  }

  @Test
  void extractClientIdFromUrl_shouldReturnNull_whenNoClientIdParam() throws Exception {
    Method method =
        SoundcloudClientIdProviderImpl.class.getDeclaredMethod(
            "extractClientIdFromUrl", String.class);
    method.setAccessible(true);

    String url = "https://api-v2.soundcloud.com/tracks/1?foo=bar&baz=123";

    String result = (String) method.invoke(provider, url);

    assertNull(result);
  }

  @Test
  void extractClientIdFromUrl_shouldReturnNull_onInvalidUrl() throws Exception {
    Method method =
        SoundcloudClientIdProviderImpl.class.getDeclaredMethod(
            "extractClientIdFromUrl", String.class);
    method.setAccessible(true);

    // invalid URI will be caught and logged, method returns null
    String url = "http://::://bad-url";

    String result = (String) method.invoke(provider, url);

    assertNull(result);
  }
}
