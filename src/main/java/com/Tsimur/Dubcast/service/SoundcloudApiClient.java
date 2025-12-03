package com.Tsimur.Dubcast.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface SoundcloudApiClient {
    JsonNode getTrack(long trackId);


    /** /resolve?url=... */
    JsonNode resolveByUrl(String url);

}