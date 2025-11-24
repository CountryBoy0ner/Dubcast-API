package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.service.RadioProgrammingService;
import com.Tsimur.Dubcast.service.ScheduleEntryService;
import com.Tsimur.Dubcast.service.TrackService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class SoundCloudRadioProgrammingServiceImpl implements RadioProgrammingService {
    private ParserService parserService;
    private TrackService trackService;
    private final ScheduleEntryService scheduleEntryService;


    @Override
    public TrackDto createTrackFromUrl(String soundcloudUrl) {
        TrackDto parsed = parserService.parseTracksByUrl(soundcloudUrl);
        String UrlOfParsedTrack = parsed.getSoundcloudUrl();

        return trackService.create(parsed);
    }

    @Override
    public ScheduleEntryDto createTrackFromUrlAndScheduleNow(String soundcloudUrl) {
        TrackDto track = createTrackFromUrl(soundcloudUrl);
        return scheduleEntryService.scheduleNow(track.getId());
    }

    @Override
    public ScheduleEntryDto scheduleExistingTrackNow(Long trackId) {
        return scheduleEntryService.scheduleNow(trackId);
    }
}
