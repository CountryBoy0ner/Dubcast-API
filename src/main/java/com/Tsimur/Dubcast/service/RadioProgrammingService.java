package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;


public interface RadioProgrammingService { //orchestrator

    TrackDto createTrackFromUrl(String soundcloudUrl);

    ScheduleEntryDto createTrackFromUrlAndScheduleNow(String soundcloudUrl);

    ScheduleEntryDto scheduleExistingTrackNow(Long trackId);

}
