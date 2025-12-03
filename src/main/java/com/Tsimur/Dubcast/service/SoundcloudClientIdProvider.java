package com.Tsimur.Dubcast.service;

public interface SoundcloudClientIdProvider {
    String getClientId();
    void invalidate();
}
