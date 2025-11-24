package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;


public interface ParserService {
    TrackDto parseTracksByUrl(String url);
    Integer getDurationSecondsByUrl(String url);
}
