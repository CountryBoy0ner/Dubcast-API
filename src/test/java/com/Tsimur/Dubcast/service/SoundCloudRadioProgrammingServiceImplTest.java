package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Tsimur.Dubcast.config.RadioTimeConfig;
import com.Tsimur.Dubcast.dto.AdminScheduleSlotDto;
import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.dto.response.PlaylistScheduleResponse;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.PlaylistMapper;
import com.Tsimur.Dubcast.model.Playlist;
import com.Tsimur.Dubcast.radio.autofill.AutoFillStrategy;
import com.Tsimur.Dubcast.repository.PlaylistRepository;
import com.Tsimur.Dubcast.service.impl.SoundCloudRadioProgrammingServiceImpl;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.data.domain.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SoundCloudRadioProgrammingServiceImplTest {

  @Mock private PlaylistRepository playlistRepository;

  @Mock private PlaylistMapper playlistMapper;

  @Mock private ScheduleEntryService scheduleEntryService;

  @Mock private RadioTimeConfig radioTimeConfig;

  @Mock private AutoFillStrategy autoFillStrategy;

  @InjectMocks private SoundCloudRadioProgrammingServiceImpl service;

  // ---------------------------------------------------------------------
  // appendPlaylistToSchedule
  // ---------------------------------------------------------------------

  @Test
  void appendPlaylistToSchedule_shouldReturnResponseWithPlaylistAndEntries() {
    Long playlistId = 1L;

    Playlist playlist = new Playlist();
    playlist.setId(playlistId);
    playlist.setName("My Playlist");

    PlaylistDto playlistDto =
        PlaylistDto.builder()
            .id(playlistId)
            .title("My Playlist")
            .soundcloudUrl("https://sc.com/p/1")
            .totalTracks(3)
            .build();

    Instant start = Instant.parse("2025-01-01T10:00:00Z");
    Instant end = start.plusSeconds(60);

    TrackDto trackDto =
        TrackDto.builder()
            .id(10L)
            .title("Track 1")
            .soundcloudUrl("https://sc.com/t/10")
            .durationSeconds(60)
            .artworkUrl("art-url")
            .build();

    ScheduleEntryDto entryDto =
        ScheduleEntryDto.builder()
            .id(100L)
            .track(trackDto)
            .startTime(start)
            .endTime(end)
            .playlistId(playlistId)
            .build();

    List<ScheduleEntryDto> entries = List.of(entryDto);

    when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));
    when(playlistMapper.toDto(playlist)).thenReturn(playlistDto);
    when(scheduleEntryService.appendPlaylistToTail(playlistId)).thenReturn(entries);

    PlaylistScheduleResponse result = service.appendPlaylistToSchedule(playlistId);

    assertNotNull(result);
    assertEquals(playlistDto, result.getPlaylist());
    assertEquals(entries, result.getScheduleEntries());

    verify(playlistRepository).findById(playlistId);
    verify(scheduleEntryService).appendPlaylistToTail(playlistId);
    verify(playlistMapper).toDto(playlist);
  }

  @Test
  void appendPlaylistToSchedule_shouldThrow_whenPlaylistNotFound() {
    Long playlistId = 42L;
    when(playlistRepository.findById(playlistId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.appendPlaylistToSchedule(playlistId));

    verify(scheduleEntryService, never()).appendPlaylistToTail(any());
  }

  // ---------------------------------------------------------------------
  // Delegating current / next / previous / appendTrack
  // ---------------------------------------------------------------------

  @Test
  void getCurrentSlot_shouldDelegateToScheduleEntryService() {
    OffsetDateTime now = OffsetDateTime.now();
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryService.getCurrent(now)).thenReturn(Optional.of(dto));

    Optional<ScheduleEntryDto> result = service.getCurrentSlot(now);

    assertTrue(result.isPresent());
    assertEquals(dto, result.get());

    verify(scheduleEntryService).getCurrent(now);
  }

  @Test
  void getNextSlot_shouldDelegateToScheduleEntryService() {
    OffsetDateTime now = OffsetDateTime.now();
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(2L).build();

    when(scheduleEntryService.getNext(now)).thenReturn(Optional.of(dto));

    Optional<ScheduleEntryDto> result = service.getNextSlot(now);

    assertTrue(result.isPresent());
    assertEquals(dto, result.get());

    verify(scheduleEntryService).getNext(now);
  }

  @Test
  void getPreviousSlot_shouldDelegateToScheduleEntryService() {
    OffsetDateTime now = OffsetDateTime.now();
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(3L).build();

    when(scheduleEntryService.getPrevious(now)).thenReturn(Optional.of(dto));

    Optional<ScheduleEntryDto> result = service.getPreviousSlot(now);

    assertTrue(result.isPresent());
    assertEquals(dto, result.get());

    verify(scheduleEntryService).getPrevious(now);
  }

  @Test
  void appendTrackToSchedule_shouldDelegateToScheduleEntryService() {
    Long trackId = 99L;
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(5L).build();

    when(scheduleEntryService.appendTrackToTail(trackId)).thenReturn(dto);

    ScheduleEntryDto result = service.appendTrackToSchedule(trackId);

    assertEquals(dto, result);
    verify(scheduleEntryService).appendTrackToTail(trackId);
  }

  // ---------------------------------------------------------------------
  // getDaySchedule (mapping to AdminScheduleSlotDto)
  // ---------------------------------------------------------------------

  @Test
  void getDaySchedule_shouldMapScheduleEntriesToAdminSlots_usingPlaylistCache() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    Pageable pageable = PageRequest.of(0, 10);
    ZoneId zoneId = ZoneId.of("Europe/Vilnius");

    when(radioTimeConfig.getRadioZoneId()).thenReturn(zoneId);

    Instant start = Instant.parse("2025-01-01T10:00:00Z");
    Instant end = start.plusSeconds(120);

    TrackDto trackDto =
        TrackDto.builder()
            .id(1L)
            .title("Track A")
            .soundcloudUrl("https://sc.com/t/1")
            .durationSeconds(120)
            .artworkUrl("art-a")
            .build();

    Long playlistId = 10L;

    ScheduleEntryDto seDto =
        ScheduleEntryDto.builder()
            .id(100L)
            .track(trackDto)
            .startTime(start)
            .endTime(end)
            .playlistId(playlistId)
            .build();

    Page<ScheduleEntryDto> sePage = new PageImpl<>(List.of(seDto), pageable, 1);

    Playlist playlist = new Playlist();
    playlist.setId(playlistId);
    playlist.setName("Morning Playlist");

    when(scheduleEntryService.getDayPage(date, pageable)).thenReturn(sePage);
    when(playlistRepository.findAllById(Set.of(playlistId))).thenReturn(List.of(playlist));

    Page<AdminScheduleSlotDto> result = service.getDaySchedule(date, pageable);

    assertEquals(1, result.getTotalElements());
    AdminScheduleSlotDto slot = result.getContent().get(0);

    assertEquals(seDto.getId(), slot.getId());
    assertEquals("Track A", slot.getTrackTitle());
    assertEquals("https://sc.com/t/1", slot.getTrackScUrl());
    assertEquals("art-a", slot.getTrackArtworkUrl());
    assertEquals(playlistId, slot.getPlaylistId());
    assertEquals("Morning Playlist", slot.getPlaylistName());

    assertEquals(seDto.getStartTime(), slot.getStartTime().toInstant());
    assertEquals(seDto.getEndTime(), slot.getEndTime().toInstant());

    verify(scheduleEntryService).getDayPage(date, pageable);
    verify(playlistRepository).findAllById(Set.of(playlistId));
  }

  // ---------------------------------------------------------------------
  // delete / insert / change / reorder - just delegation + mapping
  // ---------------------------------------------------------------------

  @Test
  void deleteSlotAndRebuildDay_shouldDelegate() {
    Long slotId = 123L;

    service.deleteSlotAndRebuildDay(slotId);

    verify(scheduleEntryService).deleteSlotAndRebuildDay(slotId);
  }

  @Test
  void insertTrackIntoDay_shouldMapToAdminSlotDto_withoutPlaylistNameLookupWhenPlaylistNull() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    Long trackId = 7L;
    int position = 2;
    ZoneId zoneId = ZoneId.of("Europe/Vilnius");
    when(radioTimeConfig.getRadioZoneId()).thenReturn(zoneId);

    Instant start = Instant.parse("2025-01-01T12:00:00Z");
    Instant end = start.plusSeconds(90);

    TrackDto trackDto =
        TrackDto.builder()
            .id(7L)
            .title("Inserted Track")
            .soundcloudUrl("https://sc.com/t/7")
            .durationSeconds(90)
            .artworkUrl("art-7")
            .build();

    ScheduleEntryDto seDto =
        ScheduleEntryDto.builder()
            .id(777L)
            .track(trackDto)
            .startTime(start)
            .endTime(end)
            .playlistId(null)
            .build();

    when(scheduleEntryService.insertTrackIntoDay(date, trackId, position)).thenReturn(seDto);

    AdminScheduleSlotDto result = service.insertTrackIntoDay(date, trackId, position);

    assertEquals(seDto.getId(), result.getId());
    assertEquals("Inserted Track", result.getTrackTitle());
    assertNull(result.getPlaylistId());
    assertNull(result.getPlaylistName());

    assertEquals(seDto.getStartTime(), result.getStartTime().toInstant());
    assertEquals(seDto.getEndTime(), result.getEndTime().toInstant());

    verify(scheduleEntryService).insertTrackIntoDay(date, trackId, position);
    verify(playlistRepository, never()).findById(any());
  }

  @Test
  void changeTrackInSlot_shouldMapToAdminSlotDto() {
    Long slotId = 5L;
    Long newTrackId = 9L;
    ZoneId zoneId = ZoneId.of("Europe/Vilnius");
    when(radioTimeConfig.getRadioZoneId()).thenReturn(zoneId);

    Instant start = Instant.parse("2025-01-02T15:00:00Z");
    Instant end = start.plusSeconds(200);

    TrackDto trackDto =
        TrackDto.builder()
            .id(9L)
            .title("New Track")
            .soundcloudUrl("https://sc.com/t/9")
            .durationSeconds(200)
            .artworkUrl("art-9")
            .build();

    ScheduleEntryDto seDto =
        ScheduleEntryDto.builder()
            .id(slotId)
            .track(trackDto)
            .startTime(start)
            .endTime(end)
            .playlistId(null)
            .build();

    when(scheduleEntryService.changeTrackInSlot(slotId, newTrackId)).thenReturn(seDto);

    AdminScheduleSlotDto result = service.changeTrackInSlot(slotId, newTrackId);

    assertEquals(slotId, result.getId());
    assertEquals("New Track", result.getTrackTitle());
    assertEquals("https://sc.com/t/9", result.getTrackScUrl());
    assertEquals("art-9", result.getTrackArtworkUrl());

    verify(scheduleEntryService).changeTrackInSlot(slotId, newTrackId);
  }

  @Test
  void reorderDay_shouldDelegateToScheduleEntryService() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    List<Long> orderedIds = List.of(1L, 2L, 3L);

    service.reorderDay(date, orderedIds);

    verify(scheduleEntryService).reorderDay(date, orderedIds);
  }

  // ---------------------------------------------------------------------
  // ensureAutofillIfNeeded
  // ---------------------------------------------------------------------

  @Test
  void ensureAutofillIfNeeded_shouldReturnFalse_whenCurrentSlotExists() {
    OffsetDateTime now = OffsetDateTime.now();
    when(scheduleEntryService.getCurrent(now))
        .thenReturn(Optional.of(ScheduleEntryDto.builder().id(1L).build()));

    boolean result = service.ensureAutofillIfNeeded(now);

    assertFalse(result);
    verify(scheduleEntryService, never()).getNext(any());
    verify(autoFillStrategy, never()).chooseTrackIdForAutofill(any());
    verify(scheduleEntryService, never()).appendTrackToTail(any());
  }

  @Test
  void ensureAutofillIfNeeded_shouldReturnFalse_whenNextSlotExists() {
    OffsetDateTime now = OffsetDateTime.now();
    when(scheduleEntryService.getCurrent(now)).thenReturn(Optional.empty());
    when(scheduleEntryService.getNext(now))
        .thenReturn(Optional.of(ScheduleEntryDto.builder().id(2L).build()));

    boolean result = service.ensureAutofillIfNeeded(now);

    assertFalse(result);
    verify(autoFillStrategy, never()).chooseTrackIdForAutofill(any());
    verify(scheduleEntryService, never()).appendTrackToTail(any());
  }

  @Test
  void ensureAutofillIfNeeded_shouldReturnFalse_whenNoTrackChosen() {
    OffsetDateTime now = OffsetDateTime.now();
    when(scheduleEntryService.getCurrent(now)).thenReturn(Optional.empty());
    when(scheduleEntryService.getNext(now)).thenReturn(Optional.empty());
    when(autoFillStrategy.chooseTrackIdForAutofill(now)).thenReturn(Optional.empty());

    boolean result = service.ensureAutofillIfNeeded(now);

    assertFalse(result);
    verify(scheduleEntryService, never()).appendTrackToTail(any());
  }

  @Test
  void ensureAutofillIfNeeded_shouldAppendTrackAndReturnTrue_whenTrackChosen() {
    OffsetDateTime now = OffsetDateTime.now();
    Long trackId = 77L;

    when(scheduleEntryService.getCurrent(now)).thenReturn(Optional.empty());
    when(scheduleEntryService.getNext(now)).thenReturn(Optional.empty());
    when(autoFillStrategy.chooseTrackIdForAutofill(now)).thenReturn(Optional.of(trackId));

    boolean result = service.ensureAutofillIfNeeded(now);

    assertTrue(result);
    verify(scheduleEntryService).appendTrackToTail(trackId);
  }
}
