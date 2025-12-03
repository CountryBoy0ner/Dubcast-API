package com.Tsimur.Dubcast.service.impl;


import com.Tsimur.Dubcast.service.SoundcloudClientIdProvider;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

@Component
@Slf4j
public class SoundcloudClientIdProviderImpl implements SoundcloudClientIdProvider {

    private final AtomicReference<String> cachedClientId = new AtomicReference<>();

    @Override
    public String getClientId() {
        String existing = cachedClientId.get();
        if (existing != null) {
            return existing;
        }

        synchronized (this) {
            existing = cachedClientId.get();
            if (existing != null) {
                return existing;
            }

            String resolved = resolveClientIdWithPlaywright();
            cachedClientId.set(resolved);
            return resolved;
        }
    }

    @Override
    public void invalidate() {
        cachedClientId.set(null);
    }

    private String resolveClientIdWithPlaywright() {
        log.info("[SC-CLIENT-ID] Resolving SoundCloud client_id via Playwright...");

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(true));

            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36"));

            Page page = context.newPage();

            final String[] foundClientId = {null};

            page.onRequest(request -> {
                String url = request.url();
                if (url.contains("api-v2.soundcloud.com") && url.contains("client_id=")) {
                    String clientId = extractClientIdFromUrl(url);
                    if (clientId != null) {
                        foundClientId[0] = clientId;
                        log.info("[SC-CLIENT-ID] Found client_id = {}", clientId);
                    }
                }
            });

            page.navigate("https://soundcloud.com/discover");
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(5000); // даем странице время пострелять запросами

            browser.close();

            if (foundClientId[0] == null) {
                throw new IllegalStateException("Could not resolve SoundCloud client_id from network requests");
            }

            return foundClientId[0];
        } catch (Exception e) {
            log.error("[SC-CLIENT-ID] Failed to resolve client_id", e);
            throw new RuntimeException("Failed to resolve SoundCloud client_id", e);
        }
    }

    private String extractClientIdFromUrl(String url) {
        try {
            URI uri = URI.create(url);
            String query = uri.getRawQuery();
            if (query == null) return null;

            String[] params = query.split("&");
            for (String p : params) {
                int idx = p.indexOf('=');
                if (idx <= 0) continue;
                String name = URLDecoder.decode(p.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(p.substring(idx + 1), StandardCharsets.UTF_8);
                if ("client_id".equals(name)) {
                    return value;
                }
            }
            return null;
        } catch (Exception e) {
            log.warn("[SC-CLIENT-ID] Failed to parse client_id from url={}", url, e);
            return null;
        }
    }
}
