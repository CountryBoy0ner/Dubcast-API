package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.PlaylistTrackDto;
import com.Tsimur.Dubcast.dto.TrackDto;

import java.util.List;

public interface PlaylistService {


    PlaylistDto create(PlaylistDto dto);

    PlaylistDto getById(Long id);

    List<PlaylistDto> getAll();

    void delete(Long id);


    List<PlaylistTrackDto> getTracks(Long playlistId);

    PlaylistTrackDto addTrack(Long playlistId, Long trackId);

//    void removeTrack(Long playlistId, Long playlistTrackId); // todo add
//
//    void moveTrack(Long playlistId, Long playlistTrackId, int newPosition);

    PlaylistDto importPlaylistFromUrl(String playlistUrl);

}
