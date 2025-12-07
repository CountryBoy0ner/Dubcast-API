package com.Tsimur.Dubcast.service;


import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.PlaylistTrackDto;
import com.Tsimur.Dubcast.dto.TrackDto;
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
import com.Tsimur.Dubcast.service.impl.PlaylistServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlaylistServiceImpl.
 *
 * We mock all dependencies (repositories, mappers, parserService)
 * and test ONLY business logic of PlaylistServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class PlaylistServiceImplTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private PlaylistTrackRepository playlistTrackRepository;

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private PlaylistMapper playlistMapper;

    @Mock
    private PlaylistTrackMapper playlistTrackMapper;

    @Mock
    private ParserService parserService;

    @Mock
    private ScheduleEntryRepository scheduleEntryRepository;

    private PlaylistService playlistService;

    @BeforeEach
    void setUp() {
        playlistService = new PlaylistServiceImpl(
                playlistRepository,
                playlistTrackRepository,
                trackRepository,
                playlistMapper,
                playlistTrackMapper,
                parserService,
                scheduleEntryRepository
        );
    }

    // ======================== create ========================

    @Test
    void create_shouldSavePlaylistAndReturnDto() {
        PlaylistDto inputDto = PlaylistDto.builder()
                .soundcloudUrl("https://soundcloud.com/user/my-playlist")
                .title("My Playlist")
                .totalTracks(0)
                .build();

        Playlist entityToSave = new Playlist();
        Playlist savedEntity = new Playlist();
        savedEntity.setId(1L);

        PlaylistDto outputDto = PlaylistDto.builder()
                .id(1L)
                .soundcloudUrl(inputDto.getSoundcloudUrl())
                .title(inputDto.getTitle())
                .totalTracks(0)
                .build();

        when(playlistMapper.toEntity(inputDto)).thenReturn(entityToSave);
        when(playlistRepository.save(entityToSave)).thenReturn(savedEntity);
        when(playlistMapper.toDto(savedEntity)).thenReturn(outputDto);

        PlaylistDto result = playlistService.create(inputDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(playlistMapper).toEntity(inputDto);
        verify(playlistRepository).save(entityToSave);
        verify(playlistMapper).toDto(savedEntity);
    }

    // ======================== getById ========================

    @Test
    void getById_shouldReturnDto_whenPlaylistExists() {
        long id = 10L;

        Playlist entity = new Playlist();
        entity.setId(id);

        PlaylistDto dto = PlaylistDto.builder()
                .id(id)
                .soundcloudUrl("url")
                .title("title")
                .totalTracks(0)
                .build();

        when(playlistRepository.findById(id)).thenReturn(Optional.of(entity));
        when(playlistMapper.toDto(entity)).thenReturn(dto);

        PlaylistDto result = playlistService.getById(id);

        assertEquals(id, result.getId());
        verify(playlistRepository).findById(id);
        verify(playlistMapper).toDto(entity);
    }

    @Test
    void getById_shouldThrow_whenPlaylistNotFound() {
        long id = 42L;
        when(playlistRepository.findById(id)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> playlistService.getById(id)
        );

        assertTrue(ex.getMessage().contains("Playlist not found"));
    }

    // ======================== getAll ========================

    @Test
    void getAll_shouldReturnMappedDtos() {
        Playlist p1 = new Playlist();
        p1.setId(1L);
        Playlist p2 = new Playlist();
        p2.setId(2L);

        PlaylistDto dto1 = PlaylistDto.builder().id(1L).soundcloudUrl("u1").title("t1").totalTracks(1).build();
        PlaylistDto dto2 = PlaylistDto.builder().id(2L).soundcloudUrl("u2").title("t2").totalTracks(2).build();

        when(playlistRepository.findAll()).thenReturn(List.of(p1, p2));
        when(playlistMapper.toDto(p1)).thenReturn(dto1);
        when(playlistMapper.toDto(p2)).thenReturn(dto2);

        List<PlaylistDto> result = playlistService.getAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());
        verify(playlistRepository).findAll();
        verify(playlistMapper, times(2)).toDto(any(Playlist.class));
    }

    // ======================== delete ========================

    @Test
    void delete_shouldDeleteFutureScheduleAndPlaylist_whenExists() {
        long playlistId = 5L;

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

        playlistService.delete(playlistId);

        verify(playlistRepository).findById(playlistId);
        verify(scheduleEntryRepository).deleteByPlaylistIdAndStartTimeAfter(
                eq(playlistId),
                any(OffsetDateTime.class)
        );
        verify(playlistRepository).delete(playlist);
    }

    @Test
    void delete_shouldThrow_whenPlaylistNotFound() {
        long playlistId = 5L;
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> playlistService.delete(playlistId)
        );

        assertTrue(ex.getMessage().contains("Playlist not found"));
        verify(scheduleEntryRepository, never())
                .deleteByPlaylistIdAndStartTimeAfter(anyLong(), any());
        verify(playlistRepository, never()).delete(any());
    }

    // ======================== getTracks ========================

    @Test
    void getTracks_shouldReturnTracks_whenPlaylistExists() {
        long playlistId = 7L;

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);

        PlaylistTrack pt1 = new PlaylistTrack();
        PlaylistTrack pt2 = new PlaylistTrack();

        PlaylistTrackDto dto1 = PlaylistTrackDto.builder().id(1L).position(0).build();
        PlaylistTrackDto dto2 = PlaylistTrackDto.builder().id(2L).position(1).build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId))
                .thenReturn(List.of(pt1, pt2));

        when(playlistTrackMapper.toDto(pt1)).thenReturn(dto1);
        when(playlistTrackMapper.toDto(pt2)).thenReturn(dto2);

        List<PlaylistTrackDto> result = playlistService.getTracks(playlistId);

        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getPosition());
        assertEquals(1, result.get(1).getPosition());

        verify(playlistRepository).findById(playlistId);
        verify(playlistTrackRepository).findByPlaylistIdOrderByPositionAsc(playlistId);
        verify(playlistTrackMapper, times(2)).toDto(any(PlaylistTrack.class));
    }

    @Test
    void getTracks_shouldThrow_whenPlaylistNotFound() {
        long playlistId = 7L;
        when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> playlistService.getTracks(playlistId)
        );

        assertTrue(ex.getMessage().contains("Playlist not found"));
        verify(playlistTrackRepository, never()).findByPlaylistIdOrderByPositionAsc(anyLong());
    }

    // ======================== addTrack ========================

    @Test
    void addTrack_shouldAppendTrackAtNextPosition() {
        long playlistId = 10L;
        long trackId = 20L;

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);

        Track track = new Track();
        track.setId(trackId);

        PlaylistTrack savedPt = new PlaylistTrack();
        savedPt.setId(100L);
        savedPt.setPlaylist(playlist);
        savedPt.setTrack(track);
        savedPt.setPosition(3);

        PlaylistTrackDto dto = PlaylistTrackDto.builder()
                .id(100L)
                .position(3)
                .build();

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(playlistTrackRepository.findMaxPositionByPlaylistId(playlistId)).thenReturn(2);
        when(playlistTrackRepository.save(any(PlaylistTrack.class))).thenReturn(savedPt);
        when(playlistTrackMapper.toDto(savedPt)).thenReturn(dto);

        PlaylistTrackDto result = playlistService.addTrack(playlistId, trackId);

        assertEquals(100L, result.getId());
        assertEquals(3, result.getPosition()); // old max=2 => new=3

        verify(playlistRepository).findById(playlistId);
        verify(trackRepository).findById(trackId);
        verify(playlistTrackRepository).findMaxPositionByPlaylistId(playlistId);
        verify(playlistTrackRepository).save(any(PlaylistTrack.class));
    }

    @Test
    void addTrack_shouldStartFromZero_whenPlaylistHasNoTracks() {
        long playlistId = 10L;
        long trackId = 20L;

        Playlist playlist = new Playlist();
        playlist.setId(playlistId);

        Track track = new Track();
        track.setId(trackId);

        ArgumentCaptor<PlaylistTrack> captor = ArgumentCaptor.forClass(PlaylistTrack.class);

        when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
        when(playlistTrackRepository.findMaxPositionByPlaylistId(playlistId)).thenReturn(null);
        when(playlistTrackRepository.save(any(PlaylistTrack.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playlistTrackMapper.toDto(any(PlaylistTrack.class)))
                .thenReturn(PlaylistTrackDto.builder().id(1L).position(0).build());

        PlaylistTrackDto result = playlistService.addTrack(playlistId, trackId);

        assertEquals(0, result.getPosition());

        verify(playlistTrackRepository).save(captor.capture());
        assertEquals(0, captor.getValue().getPosition());
    }

    @Test
    void addTrack_shouldThrow_whenPlaylistNotFound() {
        when(playlistRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> playlistService.addTrack(1L, 2L)
        );

        assertTrue(ex.getMessage().contains("Playlist not found"));
        verify(trackRepository, never()).findById(anyLong());
    }

    // ======================== importPlaylistFromUrl ========================

    @Test
    void importPlaylistFromUrl_shouldThrow_whenPlaylistAlreadyExists() {
        String url = "https://soundcloud.com/user/my-playlist";
        when(playlistRepository.findByScPlaylistUrl(url))
                .thenReturn(Optional.of(new Playlist()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> playlistService.importPlaylistFromUrl(url)
        );

        assertTrue(ex.getMessage().contains("Playlist already exists"));
        verify(parserService, never()).parsePlaylistByUrl(anyString());
    }

    @Test
    void importPlaylistFromUrl_shouldThrow_whenParsedPlaylistIsEmpty() {
        String url = "https://soundcloud.com/user/my-playlist";

        when(playlistRepository.findByScPlaylistUrl(url)).thenReturn(Optional.empty());
        when(parserService.parsePlaylistByUrl(url)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> playlistService.importPlaylistFromUrl(url)
        );

        assertTrue(ex.getMessage().contains("Playlist is empty or cannot be parsed"));
    }

    @Test
    void importPlaylistFromUrl_success() {
        String url = "https://soundcloud.com/user/my-cool-playlist";

        // 1) no existing playlist with this URL
        when(playlistRepository.findByScPlaylistUrl(url)).thenReturn(Optional.empty());

        // 2) parsed tracks from ParserService
        TrackDto t1 = TrackDto.builder()
                .soundcloudUrl("https://soundcloud.com/user/track1")
                .title("Track 1")
                .durationSeconds(120)
                .artworkUrl("art1")
                .build();

        TrackDto t2 = TrackDto.builder()
                .soundcloudUrl("https://soundcloud.com/user/track2")
                .title("Track 2")
                .durationSeconds(150)
                .artworkUrl("art2")
                .build();

        when(parserService.parsePlaylistByUrl(url)).thenReturn(List.of(t1, t2));

        // 3) saving Playlist
        Playlist savedPlaylist = new Playlist();
        savedPlaylist.setId(10L);
        savedPlaylist.setScPlaylistUrl(url);
        savedPlaylist.setName("my cool playlist");

        when(playlistRepository.save(any(Playlist.class)))
                .thenReturn(savedPlaylist);

        // 4) Track repository: both tracks are new
        when(trackRepository.findByScUrl(anyString())).thenReturn(Optional.empty());
        when(trackRepository.save(any(Track.class)))
                .thenAnswer(inv -> {
                    Track track = inv.getArgument(0);
                    if (track.getId() == null) {
                        track.setId(100L);
                    }
                    return track;
                });

        // 5) PlaylistTrackRepository: just return the entity itself
        when(playlistTrackRepository.save(any(PlaylistTrack.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // 6) Reloaded playlist for final mapping
        Playlist reloaded = new Playlist();
        reloaded.setId(10L);
        reloaded.setScPlaylistUrl(url);
        reloaded.setName("my cool playlist");

        when(playlistRepository.findById(10L)).thenReturn(Optional.of(reloaded));

        PlaylistDto resultDto = PlaylistDto.builder()
                .id(10L)
                .soundcloudUrl(url)
                .title("my cool playlist")
                .totalTracks(2)
                .build();

        when(playlistMapper.toDto(reloaded)).thenReturn(resultDto);

        // when
        PlaylistDto result = playlistService.importPlaylistFromUrl(url);

        // then
        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(url, result.getSoundcloudUrl());
        assertEquals("my cool playlist", result.getTitle());
        assertEquals(2, result.getTotalTracks());

        // verify playlist was created with extracted name from URL
        ArgumentCaptor<Playlist> playlistCaptor = ArgumentCaptor.forClass(Playlist.class);
        verify(playlistRepository).save(playlistCaptor.capture());
        Playlist created = playlistCaptor.getValue();
        assertEquals(url, created.getScPlaylistUrl());
        // "my-cool-playlist" -> "my cool playlist"
        assertEquals("my cool playlist", created.getName());

        // verify tracks are processed
        verify(parserService).parsePlaylistByUrl(url);
        verify(trackRepository, times(2)).findByScUrl(anyString());
        verify(playlistTrackRepository, times(2)).save(any(PlaylistTrack.class));
        verify(playlistRepository).findById(10L);
        verify(playlistMapper).toDto(reloaded);
    }

}
