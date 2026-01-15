package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.SoundcloudOEmbedResponse;
import com.Tsimur.Dubcast.service.impl.ParserScServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class ParserScServiceImplTest {

  @Mock private RestTemplate restTemplate;
  @Mock private SoundcloudApiClient soundcloudApiClient;

  private ObjectMapper objectMapper;

  private ParserScServiceImpl service;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    service = Mockito.spy(new ParserScServiceImpl(restTemplate, objectMapper, soundcloudApiClient));
    ReflectionTestUtils.setField(service, "oEmbedBaseUrl", "https://soundcloud.com/oembed");
  }

  // ==================== parseTracksByUrl ====================

  @Test
  void parseTracksByUrl_success() {
    String trackUrl = "https://soundcloud.com/artist/track";
    String expectedOEmbedUrl = "https://soundcloud.com/oembed?format=json&url=" + trackUrl;

    SoundcloudOEmbedResponse oembed = new SoundcloudOEmbedResponse();
    oembed.setTitle("Cool Track");
    oembed.setThumbnail_url("https://img.example/art.jpg");

    when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
        .thenReturn(oembed);

    doReturn(180).when(service).getDurationSecondsByUrl(trackUrl);

    TrackDto result = service.parseTracksByUrl(trackUrl);

    assertNotNull(result);
    assertEquals(trackUrl, result.getSoundcloudUrl());
    assertEquals("Cool Track", result.getTitle());
    assertEquals(180, result.getDurationSeconds());
    assertEquals("https://img.example/art.jpg", result.getArtworkUrl());
  }

  @Test
  void parseTracksByUrl_restTemplateThrows_throwsRuntimeException() {
    String trackUrl = "https://soundcloud.com/artist/track";
    String expectedOEmbedUrl = "https://soundcloud.com/oembed?format=json&url=" + trackUrl;

    when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
        .thenThrow(new RestClientException("Boom"));

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.parseTracksByUrl(trackUrl));
    assertTrue(ex.getMessage().contains("Failed to call SoundCloud oEmbed API"));
  }

  @Test
  void parseTracksByUrl_nullResponse_throwsRuntimeException() {
    String trackUrl = "https://soundcloud.com/artist/track";
    String expectedOEmbedUrl = "https://soundcloud.com/oembed?format=json&url=" + trackUrl;

    when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
        .thenReturn(null);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.parseTracksByUrl(trackUrl));
    assertTrue(ex.getMessage().contains("Empty response from SoundCloud oEmbed API"));
  }

  // ==================== getDurationSecondsByUrl ====================

  @Test
  void getDurationSecondsByUrl_usesResolveByUrlAndDurationField() throws Exception {
    String originalUrl = "https://soundcloud.com/artist/track?si=abc123";
    String cleanUrl = "https://soundcloud.com/artist/track";

    JsonNode node = new ObjectMapper().readTree("{\"duration\": 123000}");
    when(soundcloudApiClient.resolveByUrl(cleanUrl)).thenReturn(node);

    Integer seconds = service.getDurationSecondsByUrl(originalUrl);

    assertEquals(123, seconds);
    verify(soundcloudApiClient).resolveByUrl(cleanUrl);
  }

  // ==================== fetchOEmbedHtml ====================

  @Test
  void fetchOEmbedHtml_success() {
    String trackUrl = "https://soundcloud.com/artist/track";
    String expectedOEmbedUrl = "https://soundcloud.com/oembed?format=json&url=" + trackUrl;

    SoundcloudOEmbedResponse oembed = new SoundcloudOEmbedResponse();
    oembed.setHtml("<iframe>player</iframe>");

    when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
        .thenReturn(oembed);

    String html = service.fetchOEmbedHtml(trackUrl);

    assertEquals("<iframe>player</iframe>", html);
  }

  @Test
  void fetchOEmbedHtml_restTemplateThrows_throwsRuntimeException() {
    String trackUrl = "https://soundcloud.com/artist/track";
    String expectedOEmbedUrl = "https://soundcloud.com/oembed?format=json&url=" + trackUrl;

    when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
        .thenThrow(new RestClientException("Boom"));

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.fetchOEmbedHtml(trackUrl));
    assertTrue(ex.getMessage().contains("Failed to call SoundCloud oEmbed API"));
  }

  @Test
  void fetchOEmbedHtml_nullResponse_throwsRuntimeException() {
    String trackUrl = "https://soundcloud.com/artist/track";
    String expectedOEmbedUrl = "https://soundcloud.com/oembed?format=json&url=" + trackUrl;

    when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
        .thenReturn(null);

    RuntimeException ex =
        assertThrows(RuntimeException.class, () -> service.fetchOEmbedHtml(trackUrl));
    assertTrue(ex.getMessage().contains("Empty response from SoundCloud oEmbed API"));
  }

  // ==================== parsePlaylistByUrl (MOCKED Playwright) ====================

  @Test
  void parsePlaylistByUrl_noPlaylistInHydration_returnsEmpty() {
    String hydrationJson = "[]";

    try (MockedStatic<Playwright> mocked = Mockito.mockStatic(Playwright.class)) {
      // stack
      Playwright pw = mock(Playwright.class);
      BrowserType chromium = mock(BrowserType.class);
      Browser browser = mock(Browser.class);
      BrowserContext ctx = mock(BrowserContext.class);
      Page page = mock(Page.class);

      mocked.when(Playwright::create).thenReturn(pw);
      when(pw.chromium()).thenReturn(chromium);
      when(chromium.launch(any())).thenReturn(browser);
      when(browser.newContext(any())).thenReturn(ctx);
      when(ctx.newPage()).thenReturn(page);

      // page behavior
      doNothing().when(page).setDefaultTimeout(anyDouble());
      doNothing().when(page).onRequest(any());
      when(page.navigate(anyString())).thenReturn(null);
      doNothing().when(page).waitForLoadState(any(LoadState.class));
      doNothing().when(page).waitForTimeout(anyDouble());
      when(page.evaluate(anyString())).thenReturn(hydrationJson);

      List<TrackDto> res = service.parsePlaylistByUrl("https://soundcloud.com/user/sets/x");

      assertNotNull(res);
      assertTrue(res.isEmpty());

      verify(ctx).close();
      verify(browser).close();
    }
  }

  @Test
  void parsePlaylistByUrl_tracksNotArray_returnsEmpty() {
    String hydrationJson = "[{\"hydratable\":\"playlist\",\"data\":{\"tracks\":{}}}]";

    try (MockedStatic<Playwright> mocked = Mockito.mockStatic(Playwright.class)) {
      Playwright pw = mock(Playwright.class);
      BrowserType chromium = mock(BrowserType.class);
      Browser browser = mock(Browser.class);
      BrowserContext ctx = mock(BrowserContext.class);
      Page page = mock(Page.class);

      mocked.when(Playwright::create).thenReturn(pw);
      when(pw.chromium()).thenReturn(chromium);
      when(chromium.launch(any())).thenReturn(browser);
      when(browser.newContext(any())).thenReturn(ctx);
      when(ctx.newPage()).thenReturn(page);

      doNothing().when(page).setDefaultTimeout(anyDouble());
      doNothing().when(page).onRequest(any());
      when(page.navigate(anyString())).thenReturn(null);
      doNothing().when(page).waitForLoadState(any(LoadState.class));
      doNothing().when(page).waitForTimeout(anyDouble());
      when(page.evaluate(anyString())).thenReturn(hydrationJson);

      List<TrackDto> res = service.parsePlaylistByUrl("https://soundcloud.com/user/sets/x");

      assertTrue(res.isEmpty());
      verify(ctx).close();
      verify(browser).close();
    }
  }

  @Test
  void parsePlaylistByUrl_noClientId_fallsBackToBuiltUrl_andParseTracksByUrl() {
    String hydrationJson =
        "[{\"hydratable\":\"playlist\",\"data\":{\"tracks\":["
            + "{\"id\":404,\"duration\":0,\"user\":{\"permalink\":\"user404\"},\"permalink\":\"track404\"}"
            + "]}}]";

    TrackDto mockedDto =
        TrackDto.builder()
            .id(null)
            .soundcloudUrl("https://soundcloud.com/user404/track404")
            .title("BUILT")
            .durationSeconds(10)
            .artworkUrl(null)
            .build();

    doReturn(mockedDto).when(service).parseTracksByUrl("https://soundcloud.com/user404/track404");

    try (MockedStatic<Playwright> mocked = Mockito.mockStatic(Playwright.class)) {
      Playwright pw = mock(Playwright.class);
      BrowserType chromium = mock(BrowserType.class);
      Browser browser = mock(Browser.class);
      BrowserContext ctx = mock(BrowserContext.class);
      Page page = mock(Page.class);

      mocked.when(Playwright::create).thenReturn(pw);
      when(pw.chromium()).thenReturn(chromium);
      when(chromium.launch(any())).thenReturn(browser);
      when(browser.newContext(any())).thenReturn(ctx);
      when(ctx.newPage()).thenReturn(page);

      doNothing().when(page).setDefaultTimeout(anyDouble());
      doNothing().when(page).onRequest(any());
      when(page.navigate(anyString())).thenReturn(null);
      doNothing().when(page).waitForLoadState(any(LoadState.class));
      doNothing().when(page).waitForTimeout(anyDouble());

      when(page.evaluate(anyString())).thenReturn(hydrationJson);
      when(page.content()).thenReturn("no client_id here");

      List<TrackDto> res = service.parsePlaylistByUrl("https://soundcloud.com/user/sets/x");

      assertEquals(1, res.size());
      assertEquals("BUILT", res.get(0).getTitle());
      assertEquals("https://soundcloud.com/user404/track404", res.get(0).getSoundcloudUrl());

      verify(service).parseTracksByUrl("https://soundcloud.com/user404/track404");
      verify(restTemplate, never())
          .getForObject(startsWith("https://api-v2.soundcloud.com/tracks/"), eq(String.class));
    }
  }
}
