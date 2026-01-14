package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.SoundcloudOEmbedResponse;
import com.Tsimur.Dubcast.service.ParserService;
import com.Tsimur.Dubcast.service.SoundcloudApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParserScServiceImpl implements ParserService {

  private static final String UA =
      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
          + "AppleWebKit/537.36 (KHTML, like Gecko) "
          + "Chrome/120.0.0.0 Safari/537.36";

  private static final Pattern CLIENT_ID_PATTERN =
      Pattern.compile("client_id\\s*[:=]\\s*\"([a-zA-Z0-9]{32})\"");

  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;
  private final SoundcloudApiClient soundcloudApiClient;

  @Value("${external.soundcloud.oembed-url:https://soundcloud.com/oembed}")
  private String oEmbedBaseUrl;

  @Override
  public TrackDto parseTracksByUrl(String url) {
    String oEmbedUrl =
        UriComponentsBuilder.fromHttpUrl(oEmbedBaseUrl)
            .queryParam("format", "json")
            .queryParam("url", url)
            .toUriString();

    SoundcloudOEmbedResponse response;
    try {
      response = restTemplate.getForObject(oEmbedUrl, SoundcloudOEmbedResponse.class);
    } catch (RestClientException ex) {
      throw new RuntimeException("Failed to call SoundCloud oEmbed API: " + oEmbedUrl, ex);
    }

    if (response == null) {
      throw new RuntimeException("Empty response from SoundCloud oEmbed API for url: " + url);
    }

    Integer duration = getDurationSecondsByUrl(url);

    return TrackDto.builder()
        .id(null)
        .soundcloudUrl(url)
        .title(response.getTitle())
        .durationSeconds(duration)
        .artworkUrl(response.getThumbnail_url())
        .build();
  }

  @Override
  public Integer getDurationSecondsByUrl(String url) {
    String cleanUrl = stripQuery(url);

    try {
      JsonNode node = soundcloudApiClient.resolveByUrl(cleanUrl);

      int durationMs = node.path("duration").asInt(0);
      if (durationMs > 0) return durationMs / 1000;

      log.warn("resolveByUrl ok, but duration is 0 or missing. url={}", cleanUrl);
    } catch (Exception e) {
      log.warn("resolveByUrl failed, fallback to HTML scraping. url={}", cleanUrl, e);
    }

    Integer scraped = extractDurationSecondsByScraping(cleanUrl);
    log.warn("Using Deprecated scraping: {}", scraped);

    if (scraped != null) return scraped;

    throw new IllegalStateException("Cannot determine duration for url: " + url);
  }

  /**
   * FIX 7/7: - Read playlist + track stubs from window.__sc_hydration - Extract REAL client_id used
   * by the page (network -> fallback HTML regex) - For stub tracks call api-v2
   * /tracks/{id}?client_id=...
   */
  public List<TrackDto> parsePlaylistByUrl(String playlistUrl) {
    List<TrackDto> result = new ArrayList<>();

    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright
              .chromium()
              .launch(
                  new BrowserType.LaunchOptions()
                      .setHeadless(true)
                      .setArgs(List.of("--disable-blink-features=AutomationControlled")));

      BrowserContext context =
          browser.newContext(
              new Browser.NewContextOptions()
                  .setUserAgent(UA)
                  .setLocale("en-US")
                  .setTimezoneId("Europe/Vilnius")
                  .setViewportSize(1280, 720));

      Page page = context.newPage();
      page.setDefaultTimeout(60_000);

      AtomicReference<String> clientIdRef = new AtomicReference<>(null);

      page.onRequest(
          req -> {
            String u = req.url();
            if (clientIdRef.get() != null) return;
            if (!u.contains("api-v2.soundcloud.com")) return;
            int idx = u.indexOf("client_id=");
            if (idx < 0) return;
            String tail = u.substring(idx + "client_id=".length());
            int amp = tail.indexOf('&');
            String cid = amp >= 0 ? tail.substring(0, amp) : tail;
            if (cid.length() == 32) {
              clientIdRef.set(cid);
              log.info("[SCRAPER] captured client_id from network: {}", mask(cid));
            }
          });

      log.info("[SCRAPER] goto {}", playlistUrl);
      page.navigate(playlistUrl);
      page.waitForLoadState(LoadState.DOMCONTENTLOADED);
      page.waitForTimeout(1500);

      JsonNode playlistData = extractPlaylistDataFromHydration(page);
      if (playlistData == null) {
        log.error("[SCRAPER] playlist data not found in __sc_hydration");
        context.close();
        browser.close();
        return result;
      }

      JsonNode tracks = playlistData.path("tracks");
      if (!tracks.isArray()) {
        log.warn("[SCRAPER] playlist tracks is not array");
        context.close();
        browser.close();
        return result;
      }

      log.info("[SCRAPER] playlist.tracks size = {}", tracks.size());

      if (clientIdRef.get() == null) {
        String html = page.content();
        String cid = tryExtractClientIdFromHtml(html);
        if (cid != null) {
          clientIdRef.set(cid);
          log.info("[SCRAPER] extracted client_id from HTML: {}", mask(cid));
        } else {
          log.warn("[SCRAPER] client_id not found (network+html). stub tracks may be skipped.");
        }
      }

      final String clientId = clientIdRef.get();

      for (int i = 0; i < tracks.size(); i++) {
        JsonNode t = tracks.get(i);

        long id = t.path("id").asLong(0);

        String url = t.path("permalink_url").asText(null);
        String title = t.path("title").asText(null);
        int durationMs = t.path("duration").asInt(0);
        String artwork = t.path("artwork_url").asText(null);

        boolean ok = url != null && title != null && durationMs > 0;

        log.info(
            "[SCRAPER] track[{}] id={} ok={} (url={}, title={}, durMs={})",
            i,
            id,
            ok,
            url != null,
            title != null,
            durationMs);

        if (!ok && id > 0 && clientId != null) {
          JsonNode full = apiV2GetTrackById(id, clientId);
          if (full != null) {
            if (url == null) url = full.path("permalink_url").asText(null);
            if (title == null) title = full.path("title").asText(null);
            if (durationMs <= 0) durationMs = full.path("duration").asInt(0);
            if (artwork == null || artwork.isBlank())
              artwork = full.path("artwork_url").asText(null);
            ok = url != null && title != null && durationMs > 0;
          }
        }

        if (!ok) {
          String builtUrl = buildUrlFromHydrationStub(t);
          if (builtUrl != null) {
            try {
              TrackDto dto = parseTracksByUrl(builtUrl);
              log.info("[SCRAPER]   => OK via built URL: {}", dto.getTitle());
              result.add(dto);
              continue;
            } catch (Exception ex) {
              log.warn("[SCRAPER] built URL fallback failed: {}", ex.getMessage());
            }
          }
        }

        if (!ok) {
          log.warn("[SCRAPER]   => SKIPPED (still stub)");
          continue;
        }

        artwork = toArtworkSize(artwork, "t500x500");

        result.add(
            TrackDto.builder()
                .id(null)
                .soundcloudUrl(stripQuery(url))
                .title(title)
                .durationSeconds(durationMs / 1000)
                .artworkUrl(artwork)
                .build());
      }

      context.close();
      browser.close();

    } catch (Exception e) {
      throw new RuntimeException("Failed to parse playlist via Playwright", e);
    }

    log.info("[SCRAPER] parsed tracks: {}", result.size());
    return result;
  }

  private JsonNode extractPlaylistDataFromHydration(Page page) {
    try {
      String hydrationJson =
          page.evaluate("() => JSON.stringify(window.__sc_hydration || [])").toString();
      JsonNode hydration = objectMapper.readTree(hydrationJson);

      if (!hydration.isArray()) return null;

      for (JsonNode node : hydration) {
        if ("playlist".equals(node.path("hydratable").asText(null))) {
          return node.path("data");
        }
      }
      return null;
    } catch (Exception e) {
      log.warn("[SCRAPER] failed to read __sc_hydration: {}", e.getMessage());
      return null;
    }
  }

  private JsonNode apiV2GetTrackById(long trackId, String clientId) {
    try {
      String url = "https://api-v2.soundcloud.com/tracks/" + trackId + "?client_id=" + clientId;

      String json = restTemplate.getForObject(url, String.class);
      if (json == null || json.isBlank()) return null;

      return objectMapper.readTree(json);
    } catch (Exception e) {
      log.warn("[SCRAPER] api-v2 getTrack failed id={}, err={}", trackId, e.getMessage());
      return null;
    }
  }

  private String tryExtractClientIdFromHtml(String html) {
    if (html == null || html.isBlank()) return null;

    Matcher m = CLIENT_ID_PATTERN.matcher(html);
    if (m.find()) return m.group(1);

    String decoded = html;
    for (int i = 0; i < 2; i++) {
      try {
        decoded = URLDecoder.decode(decoded, StandardCharsets.UTF_8);
      } catch (Exception ignored) {
      }
      m = CLIENT_ID_PATTERN.matcher(decoded);
      if (m.find()) return m.group(1);
    }
    return null;
  }

  private String buildUrlFromHydrationStub(JsonNode t) {
    if (t == null) return null;

    String userPermalink = t.path("user").path("permalink").asText(null);
    String trackPermalink = t.path("permalink").asText(null);

    if (userPermalink == null || userPermalink.isBlank()) return null;
    if (trackPermalink == null || trackPermalink.isBlank()) return null;

    return "https://soundcloud.com/" + userPermalink + "/" + trackPermalink;
  }

  @Override
  public String fetchOEmbedHtml(String url) {
    String oEmbedUrl =
        UriComponentsBuilder.fromHttpUrl(oEmbedBaseUrl)
            .queryParam("format", "json")
            .queryParam("url", url)
            .toUriString();

    SoundcloudOEmbedResponse response;
    try {
      response = restTemplate.getForObject(oEmbedUrl, SoundcloudOEmbedResponse.class);
    } catch (RestClientException ex) {
      throw new RuntimeException("Failed to call SoundCloud oEmbed API: " + oEmbedUrl, ex);
    }

    if (response == null) {
      throw new RuntimeException("Empty response from SoundCloud oEmbed API for url: " + url);
    }

    log.info("ParserScServiceImpl embed -----> {}", response.getHtml());
    return response.getHtml();
  }

  @Deprecated
  private Integer extractDurationSecondsByScraping(String trackUrl) {
    try {
      Document doc =
          Jsoup.connect(trackUrl)
              .userAgent(UA)
              .referrer("https://soundcloud.com/")
              .timeout(15000)
              .get();

      Element durationMeta = doc.selectFirst("noscript article meta[itemprop=duration]");
      if (durationMeta != null) {
        Integer seconds = parseIsoDurationToSeconds(durationMeta.attr("content"));
        if (seconds != null) return seconds;
      }

      for (Element script : doc.select("script")) {
        String data = script.data();
        if (data == null || data.isEmpty()) continue;

        int idx = data.indexOf("\"duration\":");
        if (idx == -1) continue;

        int pos = idx + "\"duration\":".length();
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) pos++;

        int start = pos;
        while (pos < data.length() && Character.isDigit(data.charAt(pos))) pos++;
        if (start == pos) continue;

        long millis = Long.parseLong(data.substring(start, pos));
        long seconds = millis / 1000L;

        if (seconds > 0 && seconds < 8 * 60 * 60) return (int) seconds;
      }
      return null;
    } catch (Exception e) {
      log.warn("HTML duration scrape failed for url={}", trackUrl, e);
      return null;
    }
  }

  private Integer parseIsoDurationToSeconds(String iso) {
    if (iso == null || iso.isBlank()) return null;
    try {
      Duration d = Duration.parse(iso);
      long seconds = d.getSeconds();
      if (seconds > 0 && seconds < 8 * 60 * 60) return (int) seconds;
    } catch (Exception ignored) {
    }
    return null;
  }

  private String toArtworkSize(String url, String size) {
    if (url == null) return null;
    return url.replace("-large.", "-" + size + ".")
        .replace("-t300x300.", "-" + size + ".")
        .replace("-t500x500.", "-" + size + ".");
  }

  private String stripQuery(String url) {
    if (url == null) return null;
    int q = url.indexOf('?');
    return q > 0 ? url.substring(0, q) : url;
  }

  private String mask(String s) {
    if (s == null || s.length() < 8) return "null";
    return s.substring(0, 4) + "..." + s.substring(s.length() - 4);
  }
}
