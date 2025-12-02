package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.SoundcloudOEmbedResponse;
import com.Tsimur.Dubcast.service.ParserService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParserScServiceImpl implements ParserService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;


    @Value("${external.soundcloud.oembed-url:https://soundcloud.com/oembed}")
    private String oEmbedBaseUrl;

    @Override
    public TrackDto parseTracksByUrl(String url) {
        String oEmbedUrl = UriComponentsBuilder
                .fromHttpUrl(oEmbedBaseUrl)
                .queryParam("format", "json")
                .queryParam("url", url)
                .toUriString();

        SoundcloudOEmbedResponse response;

        try {
            response = restTemplate.getForObject(oEmbedUrl, SoundcloudOEmbedResponse.class);
        } catch (RestClientException ex) {
            throw new RuntimeException("Failed to call SoundCloud oEmbed API: " + oEmbedUrl, ex); // todo custom Exception
        }

        if (response == null) {
            throw new RuntimeException("Empty response from SoundCloud oEmbed API for url: " + url); // todo custom Exception
        }

        return TrackDto.builder()
                .id(null)
                .soundcloudUrl(url)
                .title(response.getTitle())
                .durationSeconds(extractDurationSecondsByScraping(url))
                .artworkUrl(response.getThumbnail_url())
                .build();
    }

    @Override
    public Integer getDurationSecondsByUrl(String url) {
        return extractDurationSecondsByScraping(url);
    }

    @Override
    public List<TrackDto> parsePlaylistByUrl(String playlistUrl) {
        List<TrackDto> result = new ArrayList<>();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36"));

            Page page = context.newPage();
            page.setDefaultTimeout(15_000);

            System.out.println("[SCRAPER] goto " + playlistUrl);
            page.navigate(playlistUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            String hydrationJson = page.evaluate(
                    "() => JSON.stringify(window.__sc_hydration || [])"
            ).toString();

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
                System.out.println("[SCRAPER] playlist data not found in __sc_hydration");
                browser.close();
                return result;
            }

            JsonNode tracks = playlistData.path("tracks");
            if (!tracks.isArray()) {
                System.out.println("[SCRAPER] playlist tracks is not array");
                browser.close();
                return result;
            }

            for (JsonNode t : tracks) {
                String policy = t.path("policy").asText("ALLOW");
                if (!"ALLOW".equals(policy)) {
                    continue;
                }

                String url = t.path("permalink_url").asText(null);
                String title = t.path("title").asText(null);
                int durationMs = t.path("duration").asInt(0);

                if (url == null || title == null || durationMs <= 0) {
                    continue;
                }

                String artwork = t.path("artwork_url").asText(null);

                TrackDto dto = TrackDto.builder()
                        .id(null)
                        .soundcloudUrl(url)
                        .title(title)
                        .durationSeconds(durationMs / 1000)
                        .artworkUrl(artwork)
                        .build();

                result.add(dto);
            }

            browser.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse playlist via Playwright", e);
        }

        System.out.println("[SCRAPER] parsed tracks: " + result.size());
        return result;
    }

    @Override
    public String fetchOEmbedHtml(String url) {
        String oEmbedUrl = UriComponentsBuilder
                .fromHttpUrl(oEmbedBaseUrl)
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

        log.info("ParserScServiceImpl embed ----->  "+ response.getHtml());

        return response.getHtml();
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

    private Integer extractDurationSecondsByScraping(String trackUrl) {
        try {
            Document doc = Jsoup.connect(trackUrl)
                    .userAgent("Mozilla/5.0 DubcastBot")
                    .timeout(10000)
                    .get();

            Element durationMeta = doc.selectFirst("noscript article meta[itemprop=duration]");
            if (durationMeta != null) {
                String iso = durationMeta.attr("content");
                if (iso != null && !iso.isBlank()) {
                    try {
                        Duration d = Duration.parse(iso);
                        long seconds = d.getSeconds();
                        if (seconds > 0 && seconds < 8 * 60 * 60) {
                            return (int) seconds;
                        }
                    } catch (Exception ignored) {
                    }
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
            return null;
        }
    }
}
