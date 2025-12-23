package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.*;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.PlaylistMapper;
import com.Tsimur.Dubcast.mapper.PlaylistTrackMapper;
import com.Tsimur.Dubcast.model.Playlist;
import com.Tsimur.Dubcast.model.PlaylistTrack;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.repository.PlaylistRepository;
import com.Tsimur.Dubcast.repository.PlaylistTrackRepository;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.ParserService;
import com.Tsimur.Dubcast.service.PlaylistService;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaylistServiceImpl implements PlaylistService {

  private final PlaylistRepository playlistRepository;
  private final PlaylistTrackRepository playlistTrackRepository;
  private final TrackRepository trackRepository;

  private final PlaylistMapper playlistMapper;
  private final PlaylistTrackMapper playlistTrackMapper;
  private final ParserService parserService;
  private final ScheduleEntryRepository scheduleEntryRepository;

  @Override
  public PlaylistDto create(PlaylistDto dto) {
    return playlistMapper.toDto(playlistRepository.save(playlistMapper.toEntity(dto)));
  }

  @Override
  @Transactional(readOnly = true)
  public PlaylistDto getById(Long id) {
    Playlist playlist = getPlaylistOrThrow(id);
    return playlistMapper.toDto(playlist);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlaylistDto> getAll() {
    return playlistRepository.findAll().stream().map(playlistMapper::toDto).toList();
  }

  @Transactional
  public void delete(Long playlistId) {
    Playlist playlist = getPlaylistOrThrow(playlistId);

    scheduleEntryRepository.deleteByPlaylistIdAndStartTimeAfter(playlistId, OffsetDateTime.now());

    playlistRepository.delete(playlist);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PlaylistTrackDto> getTracks(Long playlistId) {
    getPlaylistOrThrow(playlistId);

    return playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId).stream()
        .map(playlistTrackMapper::toDto)
        .toList();
  }

  @Override
  public PlaylistTrackDto addTrack(Long playlistId, Long trackId) {
    Playlist playlist = getPlaylistOrThrow(playlistId);
    Track track =
        trackRepository
            .findById(trackId)
            .orElseThrow(
                () -> new NotFoundException("Track not found: " + trackId)); // todo refactor .of

    Integer maxPos = playlistTrackRepository.findMaxPositionByPlaylistId(playlistId);
    int newPos = (maxPos == null ? 0 : maxPos + 1);

    PlaylistTrack entity =
        PlaylistTrack.builder().playlist(playlist).track(track).position(newPos).build();

    PlaylistTrack saved = playlistTrackRepository.save(entity);
    return playlistTrackMapper.toDto(saved);
  }

  @Override
  @Transactional
  public PlaylistDto importPlaylistFromUrl(String playlistUrl) {
    if (playlistRepository.findByScPlaylistUrl(playlistUrl).isPresent()) {
      throw new IllegalArgumentException("Playlist already exists: " + playlistUrl);
    }

    List<TrackDto> parsedTracks = parserService.parsePlaylistByUrl(playlistUrl);
    if (parsedTracks == null || parsedTracks.isEmpty()) {
      throw new IllegalArgumentException("Playlist is empty or cannot be parsed: " + playlistUrl);
    }

    String playlistName = extractNameFromUrl(playlistUrl);
    Playlist playlist = Playlist.builder().name(playlistName).scPlaylistUrl(playlistUrl).build();

    playlist = playlistRepository.save(playlist);

    int position = 0;
    for (TrackDto dto : parsedTracks) {

      Track track =
          trackRepository
              .findByScUrl(dto.getSoundcloudUrl())
              .orElseGet(
                  () -> {
                    Track t = new Track();
                    t.setTitle(dto.getTitle());
                    t.setScUrl(dto.getSoundcloudUrl());
                    t.setDurationSeconds(dto.getDurationSeconds());
                    t.setArtworkUrl(dto.getArtworkUrl());
                    return trackRepository.save(t);
                  });

      PlaylistTrack pt =
          PlaylistTrack.builder().playlist(playlist).track(track).position(position++).build();

      playlistTrackRepository.save(pt);
    }

    Playlist reloaded = playlistRepository.findById(playlist.getId()).orElseThrow();

    return playlistMapper.toDto(reloaded);
  }

  private String extractNameFromUrl(String url) {
    try {
      java.net.URI uri = java.net.URI.create(url);
      String path = uri.getPath();
      if (path == null || path.isBlank()) return url;
      String[] parts = path.split("/");
      String last = parts[parts.length - 1];
      if (last == null || last.isBlank()) return url;
      return last.replace('-', ' ');
    } catch (Exception e) {
      return url;
    }
  }

  private Playlist getPlaylistOrThrow(Long id) {
    return playlistRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Playlist not found: " + id)); // todo refactor .of
  }
}
