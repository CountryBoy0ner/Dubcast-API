package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.integration.soundcloud.SoundcloudOEmbedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.time.Duration;


@Service
@RequiredArgsConstructor
public class ParserServiceImpl implements ParserService {

    private final RestTemplate restTemplate;

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
            throw new RuntimeException("Failed to call SoundCloud oEmbed API: " + oEmbedUrl, ex); //todo custom Exception
        }

        if (response == null) {
            throw new RuntimeException("Empty response from SoundCloud oEmbed API for url: " + url); //todo custom Exception
        }


        return TrackDto.builder()
                .id(null)
                .soundcloudUrl(url)
                .embedCode(response.getHtml())
                .title(response.getTitle())
                .durationSeconds(extractDurationSecondsByScraping(url))
                .artworkUrl(response.getThumbnail_url())
                .build();
    }

    @Override
    public Integer getDurationSecondsByUrl(String url) {
        return extractDurationSecondsByScraping(url);
    }


    private Integer extractDurationSecondsByScraping(String trackUrl) {
        try {
            Document doc = Jsoup.connect(trackUrl)
                    .userAgent("Mozilla/5.0 DubcastBot")
                    .timeout(10000)
                    .get();

            // 1) Пытаемся вытащить <meta itemprop="duration" ...> из noscript/schema.org
            Element durationMeta = doc.selectFirst("noscript article meta[itemprop=duration]");
            if (durationMeta != null) {
                String iso = durationMeta.attr("content"); // например "PT00H05M04S"
                if (iso != null && !iso.isBlank()) {
                    try {
                        Duration d = Duration.parse(iso);        // java.time.Duration
                        long seconds = d.getSeconds();
                        if (seconds > 0 && seconds < 8 * 60 * 60) {
                            return (int) seconds;
                        }
                    } catch (Exception ignored) {
                        // если формат внезапно нестандартный — пойдём к fallback ниже
                    }
                }
            }

            // 2) Fallback: парсим JSON из window.__sc_hydration и ищем "duration":число
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

                String millisStr = data.substring(start, pos); // 304046 и т.п., в миллисекундах
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
