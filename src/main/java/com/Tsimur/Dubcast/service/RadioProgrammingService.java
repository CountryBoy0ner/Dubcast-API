package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;


public interface RadioProgrammingService { //orchestrator
    @Deprecated()//since playlist feature
    TrackDto createTrackFromUrl(String soundcloudUrl);

    @Deprecated()//since playlist feature
    ScheduleEntryDto createTrackFromUrlAndScheduleNow(String soundcloudUrl);//todo change

    @Deprecated()//since playlist feature
    ScheduleEntryDto scheduleExistingTrackNow(Long trackId);//todo change




    PlaylistScheduleResponse appendPlaylistToSchedule(Long playlistId);



}
