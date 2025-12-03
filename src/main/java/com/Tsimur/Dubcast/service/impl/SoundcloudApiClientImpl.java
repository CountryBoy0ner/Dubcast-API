package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.service.SoundcloudApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoundcloudApiClientImpl implements SoundcloudApiClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${soundcloud.client-id}")
    private String clientId;

    private static final String API_V2_BASE = "https://api-v2.soundcloud.com";

    @Override
    public JsonNode getTrack(long id) {
        String url = UriComponentsBuilder
                .fromHttpUrl(API_V2_BASE + "/tracks/" + id)
                .queryParam("client_id", clientId)
                .toUriString();

        try {
            String json = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("Failed to call getTrack({})", id, e);
            throw new RuntimeException("SoundCloud getTrack failed: " + url, e);
        }
    }

    @Override
    public JsonNode resolveByUrl(String trackUrl) {
        String url = UriComponentsBuilder
                .fromHttpUrl(API_V2_BASE + "/resolve")
                .queryParam("url", trackUrl)
                .queryParam("client_id", clientId)
                .toUriString();

        try {
            String json = restTemplate.getForObject(url, String.class);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            log.warn("Failed to resolve url={}", trackUrl, e);
            throw new RuntimeException("SoundCloud resolve failed: " + url, e);
        }
    }
}
