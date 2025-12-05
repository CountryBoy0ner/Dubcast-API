package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.PlaylistTrackDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PlaylistService {


    PlaylistDto create(PlaylistDto dto);

    PlaylistDto getById(Long id);

    List<PlaylistDto> getAll();

    void delete(Long id);


    List<PlaylistTrackDto> getTracks(Long playlistId);

    PlaylistTrackDto addTrack(Long playlistId, Long trackId);

//

    PlaylistDto importPlaylistFromUrl(String playlistUrl);



}
