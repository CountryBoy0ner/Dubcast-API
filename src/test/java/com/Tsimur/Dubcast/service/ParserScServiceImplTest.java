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
import com.sun.net.httpserver.HttpServer;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
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

  private record HttpTestServer(HttpServer server, String url, AtomicReference<String> requestedUri)
      implements AutoCloseable {
    @Override
    public void close() {
      server.stop(0);
    }
  }

  private static HttpTestServer startHtmlServer(String path, String html) throws Exception {
    AtomicReference<String> requested = new AtomicReference<>(null);

    HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
    server.createContext(
        path,
        exchange -> {
          requested.set(exchange.getRequestURI().toString());
          byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
          exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
          exchange.sendResponseHeaders(200, bytes.length);
          try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
          }
        });

    server.start();
    int port = server.getAddress().getPort();
    String url = "http://localhost:" + port + path;
    return new HttpTestServer(server, url, requested);
  }

  // ==================== getDurationSecondsByUrl fallback to scraping ====================

  @Test
  void getDurationSecondsByUrl_resolveThrows_fallsBackToScrapingMeta() throws Exception {
    String html =
        "<html><body>"
            + "<noscript><article>"
            + "<meta itemprop=\"duration\" content=\"PT3M0S\"/>"
            + "</article></noscript>"
            + "</body></html>";

    try (HttpTestServer srv = startHtmlServer("/track", html)) {
      when(soundcloudApiClient.resolveByUrl(anyString()))
          .thenThrow(new RuntimeException("resolve failed"));

      Integer seconds = service.getDurationSecondsByUrl(srv.url() + "?si=abc");

      assertEquals(180, seconds);
      // важная проверка: stripQuery реально отработал, запрос пошел без ?si=...
      assertEquals("/track", srv.requestedUri().get());
    }
  }

  @Test
  void getDurationSecondsByUrl_resolveReturnsZero_fallsBackToScrapingScriptDuration()
      throws Exception {
    String html =
        "<html><body>" + "<script>var x={\"duration\":123000};</script>" + "</body></html>";

    try (HttpTestServer srv = startHtmlServer("/track", html)) {
      JsonNode node = new ObjectMapper().readTree("{\"duration\":0}");
      when(soundcloudApiClient.resolveByUrl(anyString())).thenReturn(node);

      Integer seconds = service.getDurationSecondsByUrl(srv.url());

      assertEquals(123, seconds);
    }
  }

  @Test
  void getDurationSecondsByUrl_scrapingReturnsNull_throwsIllegalState() throws Exception {
    String html = "<html><body><h1>no duration here</h1></body></html>";

    try (HttpTestServer srv = startHtmlServer("/track", html)) {
      when(soundcloudApiClient.resolveByUrl(anyString()))
          .thenThrow(new RuntimeException("resolve failed"));

      IllegalStateException ex =
          assertThrows(
              IllegalStateException.class, () -> service.getDurationSecondsByUrl(srv.url()));

      assertTrue(ex.getMessage().contains("Cannot determine duration"));
    }
  }

  // ==================== parsePlaylistByUrl: client_id from network + api-v2 fallback
  // ====================

  @Test
  void parsePlaylistByUrl_clientIdCapturedFromNetwork_usesApiV2_andBuildsDto() {
    String cid = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 32 chars

    // stub track: ok=false -> должен пойти в api-v2 по id
    String hydrationJson =
        "[{\"hydratable\":\"playlist\",\"data\":{\"tracks\":["
            + "{\"id\":404,\"duration\":0}"
            + "]}}]";

    String apiJson =
        "{"
            + "\"permalink_url\":\"https://soundcloud.com/user404/track404?si=zzz\","
            + "\"title\":\"FromApiV2\","
            + "\"duration\":120000,"
            + "\"artwork_url\":\"https://i1.sndcdn.com/artworks-0000-large.jpg\""
            + "}";

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

      // важно: симулируем network request с client_id
      doAnswer(
              inv -> {
                @SuppressWarnings("unchecked")
                java.util.function.Consumer<Request> handler =
                    (java.util.function.Consumer<Request>) inv.getArgument(0);

                Request req = mock(Request.class);
                when(req.url())
                    .thenReturn("https://api-v2.soundcloud.com/some?client_id=" + cid + "&x=1");
                handler.accept(req);
                return null;
              })
          .when(page)
          .onRequest(any());

      when(page.navigate(anyString())).thenReturn(null);
      doNothing().when(page).waitForLoadState(any(LoadState.class));
      doNothing().when(page).waitForTimeout(anyDouble());
      when(page.evaluate(anyString())).thenReturn(hydrationJson);

      String expectedApiUrl = "https://api-v2.soundcloud.com/tracks/404?client_id=" + cid;
      when(restTemplate.getForObject(eq(expectedApiUrl), eq(String.class))).thenReturn(apiJson);

      List<TrackDto> res = service.parsePlaylistByUrl("https://soundcloud.com/user/sets/x");

      assertEquals(1, res.size());
      TrackDto dto = res.get(0);

      assertEquals("FromApiV2", dto.getTitle());
      assertEquals(120, dto.getDurationSeconds());

      // stripQuery должен убрать ?si=...
      assertEquals("https://soundcloud.com/user404/track404", dto.getSoundcloudUrl());

      // toArtworkSize должен заменить -large. -> -t500x500.
      assertEquals("https://i1.sndcdn.com/artworks-0000-t500x500.jpg", dto.getArtworkUrl());

      verify(restTemplate).getForObject(eq(expectedApiUrl), eq(String.class));
      verify(service, never()).parseTracksByUrl(anyString());

      verify(ctx).close();
      verify(browser).close();
    }
  }

  // ==================== parsePlaylistByUrl: invalid hydration json ====================

  @Test
  void parsePlaylistByUrl_invalidHydrationJson_returnsEmptyAndCloses() {
    String hydrationJson = "not-a-json";

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

      assertNotNull(res);
      assertTrue(res.isEmpty());

      verify(ctx).close();
      verify(browser).close();
    }
  }

  // ==================== parsePlaylistByUrl: full track -> no api-v2, no parseTracksByUrl
  // ====================

  @Test
  void parsePlaylistByUrl_fullTrack_doesNotCallApiV2_or_parseTracksByUrl() {
    String hydrationJson =
        "[{\"hydratable\":\"playlist\",\"data\":{\"tracks\":["
            + "{"
            + "\"id\":777,"
            + "\"permalink_url\":\"https://soundcloud.com/user777/track777?si=q\","
            + "\"title\":\"FullFromHydration\","
            + "\"duration\":61000,"
            + "\"artwork_url\":\"https://i1.sndcdn.com/artworks-7777-large.jpg\""
            + "}"
            + "]}}]";

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
      when(page.content()).thenReturn("no client_id");

      List<TrackDto> res = service.parsePlaylistByUrl("https://soundcloud.com/user/sets/x");

      assertEquals(1, res.size());
      TrackDto dto = res.get(0);

      assertEquals("FullFromHydration", dto.getTitle());
      assertEquals(61, dto.getDurationSeconds());

      // stripQuery
      assertEquals("https://soundcloud.com/user777/track777", dto.getSoundcloudUrl());
      // resize
      assertEquals("https://i1.sndcdn.com/artworks-7777-t500x500.jpg", dto.getArtworkUrl());

      verify(restTemplate, never())
          .getForObject(startsWith("https://api-v2.soundcloud.com/tracks/"), eq(String.class));
      verify(service, never()).parseTracksByUrl(anyString());

      verify(ctx).close();
      verify(browser).close();
    }
  }
}
