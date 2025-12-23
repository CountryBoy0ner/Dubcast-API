package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.Tsimur.Dubcast.service.impl.SoundcloudApiClientImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class SoundcloudApiClientImplTest {

  @Mock private RestTemplate restTemplate;

  @Mock private ObjectMapper objectMapper;

  private SoundcloudApiClientImpl client;

  @BeforeEach
  void setUp() throws Exception {
    client = new SoundcloudApiClientImpl(restTemplate, objectMapper);

    // clientId
    Field clientIdField = SoundcloudApiClientImpl.class.getDeclaredField("clientId");
    clientIdField.setAccessible(true);
    clientIdField.set(client, "test-client-id");

    Field baseUrlField = SoundcloudApiClientImpl.class.getDeclaredField("apiBaseUrl");
    baseUrlField.setAccessible(true);
    baseUrlField.set(client, "https://api-v2.soundcloud.com");
  }

  // ------------------------------------------------------------------------
  // getTrack
  // ------------------------------------------------------------------------

  @Test
  void getTrack_shouldCallSoundcloudAndReturnJsonNode() throws Exception {
    long trackId = 123L;
    String expectedUrl = "https://api-v2.soundcloud.com/tracks/123?client_id=test-client-id";
    String json = "{\"id\":123}";
    JsonNode node = mock(JsonNode.class);

    when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(json);
    when(objectMapper.readTree(json)).thenReturn(node);

    JsonNode result = client.getTrack(trackId);

    assertSame(node, result);
    verify(restTemplate).getForObject(expectedUrl, String.class);
    verify(objectMapper).readTree(json);
  }

  @Test
  void getTrack_shouldWrapExceptionInRuntimeException() {
    long trackId = 123L;

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenThrow(new RuntimeException("boom"));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> client.getTrack(trackId));

    assertTrue(ex.getMessage().contains("SoundCloud getTrack failed"));
  }

  // ------------------------------------------------------------------------
  // resolveByUrl
  // ------------------------------------------------------------------------

  @Test
  void resolveByUrl_shouldCallResolveEndpointAndReturnJsonNode() throws Exception {
    String trackUrl = "https://soundcloud.com/some-user/some-track";
    String expectedUrl =
        "https://api-v2.soundcloud.com/resolve?url=https://soundcloud.com/some-user/some-track&client_id=test-client-id";

    String json = "{\"kind\":\"track\",\"id\":999}";
    JsonNode node = mock(JsonNode.class);

    when(restTemplate.getForObject(expectedUrl, String.class)).thenReturn(json);
    when(objectMapper.readTree(json)).thenReturn(node);

    JsonNode result = client.resolveByUrl(trackUrl);

    assertSame(node, result);
    verify(restTemplate).getForObject(expectedUrl, String.class);
    verify(objectMapper).readTree(json);
  }

  @Test
  void resolveByUrl_shouldWrapExceptionInRuntimeException() {
    String trackUrl = "https://soundcloud.com/some-user/some-track";

    when(restTemplate.getForObject(anyString(), eq(String.class)))
        .thenThrow(new RuntimeException("boom"));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> client.resolveByUrl(trackUrl));

    assertTrue(ex.getMessage().contains("SoundCloud resolve failed"));
  }
}
