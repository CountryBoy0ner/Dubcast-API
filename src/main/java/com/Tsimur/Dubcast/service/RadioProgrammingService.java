package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.TrackDto;


public interface RadioProgrammingService { //orchestrator
    TrackDto createTrackFromUrl(String soundcloudUrl);

}
