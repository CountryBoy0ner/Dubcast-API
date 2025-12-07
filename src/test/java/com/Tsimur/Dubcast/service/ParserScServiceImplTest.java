package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.SoundcloudOEmbedResponse;
import com.Tsimur.Dubcast.service.SoundcloudApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ParserScServiceImpl.
 *
 * We mock:
 *  - RestTemplate  (HTTP calls to SoundCloud oEmbed)
 *  - SoundcloudApiClient (SoundCloud API v2 / resolve)
 *
 * We DO NOT hit the network or Playwright in these tests.
 */
@ExtendWith(MockitoExtension.class)
class ParserScServiceImplTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    private SoundcloudApiClient soundcloudApiClient;

    // we use a spy to stub getDurationSecondsByUrl in parseTracksByUrl
    private ParserScServiceImpl service;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        objectMapper = new ObjectMapper(); // real mapper is fine here
        soundcloudApiClient = mock(SoundcloudApiClient.class);

        service = Mockito.spy(new ParserScServiceImpl(
                restTemplate,
                objectMapper,
                soundcloudApiClient
        ));

        // inject @Value field manually
        ReflectionTestUtils.setField(
                service,
                "oEmbedBaseUrl",
                "https://soundcloud.com/oembed"
        );
    }

    // ==================== parseTracksByUrl ====================

    @Test
    void parseTracksByUrl_success() throws Exception {
        // given
        String trackUrl = "https://soundcloud.com/artist/track";
        String expectedOEmbedUrl = "https://soundcloud.com/oembed"
                + "?format=json&url=" + trackUrl;

        SoundcloudOEmbedResponse oembed = new SoundcloudOEmbedResponse();
        oembed.setTitle("Cool Track");
        oembed.setThumbnail_url("https://img.example/art.jpg");

        when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
                .thenReturn(oembed);

        // we don't want to go через реальный getDurationSecondsByUrl (там SoundcloudApiClient + scraping)
        doReturn(180).when(service).getDurationSecondsByUrl(trackUrl);

        // when
        TrackDto result = service.parseTracksByUrl(trackUrl);

        // then
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals(trackUrl, result.getSoundcloudUrl());
        assertEquals("Cool Track", result.getTitle());
        assertEquals(180, result.getDurationSeconds());
        assertEquals("https://img.example/art.jpg", result.getArtworkUrl());

        verify(restTemplate).getForObject(expectedOEmbedUrl, SoundcloudOEmbedResponse.class);
        verify(service).getDurationSecondsByUrl(trackUrl);
    }

    @Test
    void parseTracksByUrl_restTemplateThrows_throwsRuntimeException() {
        // given
        String trackUrl = "https://soundcloud.com/artist/track";
        String expectedOEmbedUrl = "https://soundcloud.com/oembed"
                + "?format=json&url=" + trackUrl;

        when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
                .thenThrow(new RestClientException("Boom"));

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.parseTracksByUrl(trackUrl)
        );

        assertTrue(ex.getMessage().contains("Failed to call SoundCloud oEmbed API"));
    }

    @Test
    void parseTracksByUrl_nullResponse_throwsRuntimeException() {
        // given
        String trackUrl = "https://soundcloud.com/artist/track";
        String expectedOEmbedUrl = "https://soundcloud.com/oembed"
                + "?format=json&url=" + trackUrl;

        when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
                .thenReturn(null);

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.parseTracksByUrl(trackUrl)
        );

        assertTrue(ex.getMessage().contains("Empty response from SoundCloud oEmbed API"));
    }

    // ==================== getDurationSecondsByUrl ====================

    @Test
    void getDurationSecondsByUrl_usesResolveByUrlAndDurationField() throws Exception {
        // given
        String originalUrl = "https://soundcloud.com/artist/track?si=abc123";
        String cleanUrl = "https://soundcloud.com/artist/track";

        // real ObjectMapper to build JsonNode
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree("{\"duration\": 123000}");

        when(soundcloudApiClient.resolveByUrl(cleanUrl)).thenReturn(node);

        // when
        Integer seconds = service.getDurationSecondsByUrl(originalUrl);

        // then
        assertNotNull(seconds);
        assertEquals(123, seconds);

        verify(soundcloudApiClient).resolveByUrl(cleanUrl);
    }

    // ==================== fetchOEmbedHtml ====================

    @Test
    void fetchOEmbedHtml_success() {
        // given
        String trackUrl = "https://soundcloud.com/artist/track";
        String expectedOEmbedUrl = "https://soundcloud.com/oembed"
                + "?format=json&url=" + trackUrl;

        SoundcloudOEmbedResponse oembed = new SoundcloudOEmbedResponse();
        oembed.setHtml("<iframe>player</iframe>");

        when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
                .thenReturn(oembed);

        // when
        String html = service.fetchOEmbedHtml(trackUrl);

        // then
        assertEquals("<iframe>player</iframe>", html);
        verify(restTemplate).getForObject(expectedOEmbedUrl, SoundcloudOEmbedResponse.class);
    }

    @Test
    void fetchOEmbedHtml_restTemplateThrows_throwsRuntimeException() {
        // given
        String trackUrl = "https://soundcloud.com/artist/track";
        String expectedOEmbedUrl = "https://soundcloud.com/oembed"
                + "?format=json&url=" + trackUrl;

        when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
                .thenThrow(new RestClientException("Boom"));

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.fetchOEmbedHtml(trackUrl)
        );

        assertTrue(ex.getMessage().contains("Failed to call SoundCloud oEmbed API"));
    }

    @Test
    void fetchOEmbedHtml_nullResponse_throwsRuntimeException() {
        // given
        String trackUrl = "https://soundcloud.com/artist/track";
        String expectedOEmbedUrl = "https://soundcloud.com/oembed"
                + "?format=json&url=" + trackUrl;

        when(restTemplate.getForObject(eq(expectedOEmbedUrl), eq(SoundcloudOEmbedResponse.class)))
                .thenReturn(null);

        // when / then
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.fetchOEmbedHtml(trackUrl)
        );

        assertTrue(ex.getMessage().contains("Empty response from SoundCloud oEmbed API"));
    }

    // ==================== parsePlaylistByUrl ====================
    //
    // Intentionally NOT unit-tested here, because it:
    //  - Uses Playwright (browser automation, JS execution)
    //  - Requires either static mocking (Playwright.create) or real network
    //
    // This part is better covered by integration / system tests,
    // not by pure unit tests with Mockito.
    //
}
