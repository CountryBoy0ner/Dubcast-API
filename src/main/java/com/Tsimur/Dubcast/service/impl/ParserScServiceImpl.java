package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.SoundcloudOEmbedResponse;
import com.Tsimur.Dubcast.service.ParserService;
import com.Tsimur.Dubcast.service.SoundcloudApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
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
    String cleanUrl = url;
    int qIdx = url.indexOf('?');
    if (qIdx > 0) {
      cleanUrl = url.substring(0, qIdx);
    }

    try {
      JsonNode node = soundcloudApiClient.resolveByUrl(cleanUrl);

      int durationMs = node.path("duration").asInt(0);
      if (durationMs > 0) {
        return durationMs / 1000;
      }
      log.warn("resolveByUrl ok, but duration is 0 or missing. url={}", cleanUrl);
    } catch (Exception e) {
      log.warn("resolveByUrl failed, fallback to HTML scraping. url={}", cleanUrl, e);
    }

    Integer scraped = extractDurationSecondsByScraping(cleanUrl);
    log.warn("Using Depricated scraping: {}", scraped);

    if (scraped != null) {
      return scraped;
    }

    throw new IllegalStateException("Cannot determine duration for url: " + url);
  }

  public List<TrackDto> parsePlaylistByUrl(String playlistUrl) {
    List<TrackDto> result = new ArrayList<>();

    try (Playwright playwright = Playwright.create()) {
      Browser browser =
          playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

      BrowserContext context =
          browser.newContext(
              new Browser.NewContextOptions()
                  .setUserAgent(
                      "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                          + "AppleWebKit/537.36 (KHTML, like Gecko) "
                          + "Chrome/120.0.0.0 Safari/537.36"));

      Page page = context.newPage();
      page.setDefaultTimeout(30_000);

      System.out.println("[SCRAPER] goto " + playlistUrl);
      page.navigate(playlistUrl);
      page.waitForLoadState(LoadState.NETWORKIDLE);

      String hydrationJson =
          page.evaluate("() => JSON.stringify(window.__sc_hydration || [])").toString();

      JsonNode hydration = objectMapper.readTree(hydrationJson);

      JsonNode playlistData = null;
      if (hydration.isArray()) {
        for (JsonNode node : hydration) {
          String hydratable = node.path("hydratable").asText(null);
          if ("playlist".equals(hydratable)) {
            playlistData = node.path("data");
            break;
          }
        }
      }

      if (playlistData == null) {
        log.error("[SCRAPER] playlist data not found in __sc_hydration");
        browser.close();
        return result;
      }

      JsonNode tracks = playlistData.path("tracks");
      if (!tracks.isArray()) {
        log.info("[SCRAPER] playlist tracks is not array");
        browser.close();
        return result;
      }

      log.info("[SCRAPER] playlist.tracks size = {}", tracks.size());

      int index = 0;
      for (JsonNode t : tracks) {
        long id = t.path("id").asLong(0);
        boolean hasUrl = t.hasNonNull("permalink_url");
        boolean hasTitle = t.hasNonNull("title");
        boolean hasDuration = t.has("duration") && t.get("duration").asInt(0) > 0;

        log.info(
            "[SCRAPER] track[%d] id=%d hasUrl=%s hasTitle=%s hasDuration=%s%n",
            new Object[] {index++, id, hasUrl, hasTitle, hasDuration});

        TrackDto dto = buildTrackFromNodeOrFetch(t);
        if (dto != null) {
          log.info("[SCRAPER]   => OK: {}", dto.getTitle());
          result.add(dto);
        } else {
          log.warn("[SCRAPER]   => SKIPPED");
        }
      }

      browser.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to parse playlist via Playwright", e);
    }

    log.info("[SCRAPER] parsed tracks: {}", result.size());
    return result;
  }

  private TrackDto buildTrackFromNodeOrFetch(JsonNode t) {
    long id = t.path("id").asLong(0);

    String url = t.path("permalink_url").asText(null);
    String title = t.path("title").asText(null);
    int durationMs = t.path("duration").asInt(0);

    String artwork = t.path("artwork_url").asText(null);

    boolean hasEnough = url != null && title != null && durationMs > 0;

    if (!hasEnough && id != 0) {
      log.info("[SCRAPER]   stub track id={} -> fetching full JSON via api-v2...", id);
      try {
        JsonNode full = soundcloudApiClient.getTrack(id);

        if (url == null) url = full.path("permalink_url").asText(null);
        if (title == null) title = full.path("title").asText(null);
        if (durationMs <= 0) durationMs = full.path("duration").asInt(0);

        if (artwork == null || artwork.isBlank()) {
          artwork = full.path("artwork_url").asText(null);
        }

        hasEnough = url != null && title != null && durationMs > 0;
        if (!hasEnough) {
          log.error("[SCRAPER]   still not enough data for id={} -> skip", id);
          return null;
        }

      } catch (Exception e) {
        log.error("[SCRAPER]   exception while fetching track id={}: {}", id, e.getMessage());
        return null;
      }
    }

    if (!hasEnough) {
      log.error("[SCRAPER]   not enough data for id={} -> skip", id);
      return null;
    }

    artwork = toArtworkSize(artwork, "t500x500");

    return TrackDto.builder()
        .id(null)
        .soundcloudUrl(url)
        .title(title)
        .durationSeconds(durationMs / 1000)
        .artworkUrl(artwork)
        .build();
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

  // old HTML-scrapper for duration (fallback)
  @Deprecated
  private Integer extractDurationSecondsByScraping(String trackUrl) {
    try {
      Document doc =
          Jsoup.connect(trackUrl)
              .userAgent(
                  "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      + "AppleWebKit/537.36 (KHTML, like Gecko) "
                      + "Chrome/120.0.0.0 Safari/537.36")
              .referrer("https://soundcloud.com/")
              .timeout(15000)
              .get();

      Element durationMeta = doc.selectFirst("noscript article meta[itemprop=duration]");
      if (durationMeta != null) {
        Integer seconds = parseIsoDurationToSeconds(durationMeta.attr("content"));
        if (seconds != null) {
          return seconds;
        }
      }

      for (Element script : doc.select("script")) {
        String data = script.data();
        if (data == null || data.isEmpty()) {
          continue;
        }

        int idx = data.indexOf("\"duration\":");
        if (idx == -1) continue;

        int pos = idx + "\"duration\":".length();
        while (pos < data.length() && Character.isWhitespace(data.charAt(pos))) {
          pos++;
        }
        int start = pos;
        while (pos < data.length() && Character.isDigit(data.charAt(pos))) {
          pos++;
        }
        if (start == pos) continue;

        String millisStr = data.substring(start, pos);
        long millis = Long.parseLong(millisStr);
        long seconds = millis / 1000L;

        if (seconds > 0 && seconds < 8 * 60 * 60) {
          return (int) seconds;
        }
      }
      return null;
    } catch (Exception e) {
      log.warn("HTML duration scrape failed for url={}", trackUrl, e);
      return null;
    }
  }

  private Integer parseIsoDurationToSeconds(String iso) {
    if (iso == null || iso.isBlank()) {
      return null;
    }
    try {
      Duration d = Duration.parse(iso);
      long seconds = d.getSeconds();
      if (seconds > 0 && seconds < 8 * 60 * 60) {
        return (int) seconds;
      }
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
}
