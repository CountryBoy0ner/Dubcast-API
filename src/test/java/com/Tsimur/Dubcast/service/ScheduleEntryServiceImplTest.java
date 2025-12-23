package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.Tsimur.Dubcast.config.RadioTimeConfig;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.exception.type.SlotCurrentlyPlayingException;
import com.Tsimur.Dubcast.mapper.ScheduleEntryMapper;
import com.Tsimur.Dubcast.model.Playlist;
import com.Tsimur.Dubcast.model.PlaylistTrack;
import com.Tsimur.Dubcast.model.ScheduleEntry;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.radio.events.ScheduleUpdatedEvent;
import com.Tsimur.Dubcast.repository.PlaylistRepository;
import com.Tsimur.Dubcast.repository.PlaylistTrackRepository;
import com.Tsimur.Dubcast.repository.ScheduleEntryRepository;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.impl.ScheduleEntryServiceImpl;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;

/**
 * Unit tests for ScheduleEntryServiceImpl.
 *
 * <p>We mock repositories, mapper and event publisher. RadioTimeConfig is real (fixed timezone for
 * deterministic tests).
 */
@ExtendWith(MockitoExtension.class)
class ScheduleEntryServiceImplTest {

  @Mock private ScheduleEntryRepository scheduleEntryRepository;

  @Mock private ScheduleEntryMapper scheduleEntryMapper;

  @Mock private TrackRepository trackRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @Mock private PlaylistRepository playlistRepository;

  @Mock private PlaylistTrackRepository playlistTrackRepository;

  private RadioTimeConfig radioTimeConfig;

  private ScheduleEntryServiceImpl service;

  @BeforeEach
  void setUp() {
    // Use fixed timezone so date calculations are predictable
    this.radioTimeConfig = new RadioTimeConfig("UTC");

    this.service =
        new ScheduleEntryServiceImpl(
            scheduleEntryRepository,
            scheduleEntryMapper,
            trackRepository,
            radioTimeConfig,
            eventPublisher,
            playlistRepository,
            playlistTrackRepository);
  }

  // ----------------------------------------------------------------------
  // CRUD
  // ----------------------------------------------------------------------

  @Test
  void create_shouldSaveAndReturnDto() {
    ScheduleEntryDto input =
        ScheduleEntryDto.builder()
            .id(123L) // will be nulled inside service, but we don't care
            .track(TrackDto.builder().build())
            .startTime(Instant.now())
            .endTime(Instant.now().plusSeconds(60))
            .build();

    ScheduleEntry entity = new ScheduleEntry();
    ScheduleEntry saved = new ScheduleEntry();
    ScheduleEntryDto output = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryMapper.toEntity(input)).thenReturn(entity);
    when(scheduleEntryRepository.save(entity)).thenReturn(saved);
    when(scheduleEntryMapper.toDto(saved)).thenReturn(output);

    ScheduleEntryDto result = service.create(input);

    assertEquals(1L, result.getId());
    verify(scheduleEntryMapper).toEntity(input);
    verify(scheduleEntryRepository).save(entity);
    verify(scheduleEntryMapper).toDto(saved);
  }

  @Test
  void getById_shouldReturnDto_whenExists() {
    long id = 10L;
    ScheduleEntry entity = new ScheduleEntry();
    entity.setId(id);

    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(id).build();

    when(scheduleEntryRepository.findById(id)).thenReturn(Optional.of(entity));
    when(scheduleEntryMapper.toDto(entity)).thenReturn(dto);

    ScheduleEntryDto result = service.getById(id);

    assertEquals(id, result.getId());
    verify(scheduleEntryRepository).findById(id);
    verify(scheduleEntryMapper).toDto(entity);
  }

  @Test
  void getById_shouldThrow_whenNotFound() {
    long id = 42L;
    when(scheduleEntryRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> service.getById(id));
  }

  @Test
  void getAll_shouldReturnMappedDtos() {
    ScheduleEntry e1 = new ScheduleEntry();
    e1.setId(1L);
    ScheduleEntry e2 = new ScheduleEntry();
    e2.setId(2L);

    ScheduleEntryDto d1 = ScheduleEntryDto.builder().id(1L).build();
    ScheduleEntryDto d2 = ScheduleEntryDto.builder().id(2L).build();

    when(scheduleEntryRepository.findAll()).thenReturn(List.of(e1, e2));
    when(scheduleEntryMapper.toDtoList(List.of(e1, e2))).thenReturn(List.of(d1, d2));

    List<ScheduleEntryDto> result = service.getAll();

    assertEquals(2, result.size());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());
  }

  @Test
  void update_shouldUpdateAndReturnDto_whenExists() {
    long id = 5L;
    ScheduleEntry existing = new ScheduleEntry();
    existing.setId(id);

    ScheduleEntryDto patch =
        ScheduleEntryDto.builder()
            .id(999L) // ignored in mapper
            .build();

    ScheduleEntry saved = new ScheduleEntry();
    saved.setId(id);
    ScheduleEntryDto resultDto = ScheduleEntryDto.builder().id(id).build();

    when(scheduleEntryRepository.findById(id)).thenReturn(Optional.of(existing));
    // updateEntityFromDto is void; we simply verify it's called
    when(scheduleEntryRepository.save(existing)).thenReturn(saved);
    when(scheduleEntryMapper.toDto(saved)).thenReturn(resultDto);

    ScheduleEntryDto result = service.update(id, patch);

    assertEquals(id, result.getId());
    verify(scheduleEntryMapper).updateEntityFromDto(patch, existing);
    verify(scheduleEntryRepository).save(existing);
  }

  @Test
  void update_shouldThrow_whenNotFound() {
    when(scheduleEntryRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(
        NotFoundException.class, () -> service.update(1L, ScheduleEntryDto.builder().build()));
  }

  @Test
  void delete_shouldDelete_whenExists() {
    long id = 3L;
    when(scheduleEntryRepository.existsById(id)).thenReturn(true);

    service.delete(id);

    verify(scheduleEntryRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrow_whenNotExists() {
    when(scheduleEntryRepository.existsById(3L)).thenReturn(false);
    assertThrows(NotFoundException.class, () -> service.delete(3L));
    verify(scheduleEntryRepository, never()).deleteById(anyLong());
  }

  // ----------------------------------------------------------------------
  // current / next / previous
  // ----------------------------------------------------------------------

  @Test
  void getCurrent_shouldReturnDto_whenPresent() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    ScheduleEntry entity = new ScheduleEntry();
    entity.setId(1L);
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryRepository.findCurrent(now)).thenReturn(Optional.of(entity));
    when(scheduleEntryMapper.toDto(entity)).thenReturn(dto);

    var result = service.getCurrent(now);

    assertTrue(result.isPresent());
    assertEquals(1L, result.get().getId());
  }

  @Test
  void getCurrent_shouldReturnEmpty_whenNoCurrent() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    when(scheduleEntryRepository.findCurrent(now)).thenReturn(Optional.empty());

    var result = service.getCurrent(now);

    assertTrue(result.isEmpty());
  }

  @Test
  void getNext_shouldReturnFirstFromList() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    ScheduleEntry entity = new ScheduleEntry();
    entity.setId(2L);
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(2L).build();

    when(scheduleEntryRepository.findNext(eq(now), any(Pageable.class)))
        .thenReturn(List.of(entity));
    when(scheduleEntryMapper.toDto(entity)).thenReturn(dto);

    var result = service.getNext(now);

    assertTrue(result.isPresent());
    assertEquals(2L, result.get().getId());
  }

  @Test
  void getNext_shouldReturnEmpty_whenNoNext() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    when(scheduleEntryRepository.findNext(eq(now), any(Pageable.class))).thenReturn(List.of());

    assertTrue(service.getNext(now).isEmpty());
  }

  @Test
  void getPrevious_shouldReturnFirstFromList() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

    ScheduleEntry entity = new ScheduleEntry();
    entity.setId(3L);
    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(3L).build();

    when(scheduleEntryRepository.findPrevious(eq(now), any(Pageable.class)))
        .thenReturn(List.of(entity));
    when(scheduleEntryMapper.toDto(entity)).thenReturn(dto);

    var result = service.getPrevious(now);

    assertTrue(result.isPresent());
    assertEquals(3L, result.get().getId());
  }

  @Test
  void getPrevious_shouldReturnEmpty_whenNoPrevious() {
    OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
    when(scheduleEntryRepository.findPrevious(eq(now), any(Pageable.class))).thenReturn(List.of());

    assertTrue(service.getPrevious(now).isEmpty());
  }

  // ----------------------------------------------------------------------
  // ranges / day / pages
  // ----------------------------------------------------------------------

  @Test
  void getRange_shouldReturnMappedList() {
    OffsetDateTime from = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime to = from.plusHours(1);

    ScheduleEntry e1 = new ScheduleEntry();
    e1.setId(1L);

    ScheduleEntryDto d1 = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(from, to))
        .thenReturn(List.of(e1));
    when(scheduleEntryMapper.toDtoList(List.of(e1))).thenReturn(List.of(d1));

    List<ScheduleEntryDto> result = service.getRange(from, to);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
  }

  @Test
  void getDay_shouldUseConfiguredZoneAndReturnList() {
    LocalDate date = LocalDate.of(2025, 1, 1);

    ScheduleEntry e1 = new ScheduleEntry();
    e1.setId(1L);
    ScheduleEntryDto d1 = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(any(), any()))
        .thenReturn(List.of(e1));
    when(scheduleEntryMapper.toDtoList(List.of(e1))).thenReturn(List.of(d1));

    List<ScheduleEntryDto> result = service.getDay(date);

    assertEquals(1, result.size());
    assertEquals(1L, result.get(0).getId());
    verify(scheduleEntryRepository)
        .findByStartTimeBetweenOrderByStartTime(
            any(OffsetDateTime.class), any(OffsetDateTime.class));
  }

  @Test
  void getRangePage_shouldMapPageContent() {
    OffsetDateTime from = OffsetDateTime.now(ZoneOffset.UTC);
    OffsetDateTime to = from.plusHours(2);
    Pageable pageable = PageRequest.of(0, 10);

    ScheduleEntry e1 = new ScheduleEntry();
    e1.setId(1L);
    Page<ScheduleEntry> page = new PageImpl<>(List.of(e1));

    ScheduleEntryDto d1 = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryRepository.findPageByStartTimeBetweenOrderByStartTime(from, to, pageable))
        .thenReturn(page);
    when(scheduleEntryMapper.toDto(e1)).thenReturn(d1);

    Page<ScheduleEntryDto> result = service.getRangePage(from, to, pageable);

    assertEquals(1, result.getTotalElements());
    assertEquals(1L, result.getContent().get(0).getId());
  }

  @Test
  void getDayPage_shouldDelegateToRangePage() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    Pageable pageable = PageRequest.of(0, 10);

    ScheduleEntry e1 = new ScheduleEntry();
    e1.setId(1L);
    Page<ScheduleEntry> page = new PageImpl<>(List.of(e1));
    ScheduleEntryDto d1 = ScheduleEntryDto.builder().id(1L).build();

    when(scheduleEntryRepository.findPageByStartTimeBetweenOrderByStartTime(
            any(), any(), eq(pageable)))
        .thenReturn(page);
    when(scheduleEntryMapper.toDto(e1)).thenReturn(d1);

    Page<ScheduleEntryDto> result = service.getDayPage(date, pageable);

    assertEquals(1, result.getTotalElements());
    verify(scheduleEntryRepository)
        .findPageByStartTimeBetweenOrderByStartTime(any(), any(), eq(pageable));
  }

  // ----------------------------------------------------------------------
  // appendTrackToTail
  // ----------------------------------------------------------------------

  @Test
  void appendTrackToTail_shouldCreateEntryAndPublishEvent() {
    long trackId = 10L;

    Track track = new Track();
    track.setId(trackId);
    track.setDurationSeconds(120);

    when(trackRepository.findById(trackId)).thenReturn(Optional.of(track));
    when(scheduleEntryRepository.findMaxEndTime()).thenReturn(null);

    when(scheduleEntryRepository.save(any(ScheduleEntry.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    ScheduleEntryDto dto =
        ScheduleEntryDto.builder().id(1L).track(TrackDto.builder().build()).build();
    when(scheduleEntryMapper.toDto(any(ScheduleEntry.class))).thenReturn(dto);

    ScheduleEntryDto result = service.appendTrackToTail(trackId);

    assertEquals(1L, result.getId());

    ArgumentCaptor<ScheduleEntry> captor = ArgumentCaptor.forClass(ScheduleEntry.class);
    verify(scheduleEntryRepository).save(captor.capture());
    ScheduleEntry saved = captor.getValue();

    assertEquals(track, saved.getTrack());
    assertNull(saved.getPlaylist());

    long seconds = Duration.between(saved.getStartTime(), saved.getEndTime()).getSeconds();
    assertEquals(120, seconds);

    verify(eventPublisher).publishEvent(any(ScheduleUpdatedEvent.class));
  }

  @Test
  void appendTrackToTail_shouldThrow_whenTrackNotFound() {
    when(trackRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.appendTrackToTail(1L));
  }

  @Test
  void appendTrackToTail_shouldThrow_whenDurationInvalid() {
    Track track = new Track();
    track.setId(1L);
    track.setDurationSeconds(0);

    when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

    assertThrows(IllegalStateException.class, () -> service.appendTrackToTail(1L));
  }

  // ----------------------------------------------------------------------
  // deleteSlotAndRebuildDay
  // ----------------------------------------------------------------------

  @Test
  void deleteSlotAndRebuildDay_shouldThrow_whenCurrentlyPlaying() {
    ZoneId zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime now = OffsetDateTime.now(zone);

    ScheduleEntry entry = new ScheduleEntry();
    entry.setId(1L);
    entry.setStartTime(now.minusMinutes(10));
    entry.setEndTime(now.plusMinutes(10));

    when(scheduleEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

    SlotCurrentlyPlayingException ex =
        assertThrows(
            SlotCurrentlyPlayingException.class, () -> service.deleteSlotAndRebuildDay(1L));

    assertTrue(ex.getMessage().contains("Cannot delete currently playing slot"));
    verify(scheduleEntryRepository, never()).delete(any());
  }

  @Test
  void deleteSlotAndRebuildDay_shouldDeleteAndRebuild_whenNotCurrent() {
    ZoneId zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime now = OffsetDateTime.now(zone);

    ScheduleEntry entry = new ScheduleEntry();
    entry.setId(1L);
    entry.setStartTime(now.minusHours(2));
    entry.setEndTime(now.minusHours(1));

    when(scheduleEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

    // Day entries that will be rebuilt
    ScheduleEntry dayEntry = new ScheduleEntry();
    dayEntry.setId(2L);
    Track t = new Track();
    t.setId(5L);
    t.setDurationSeconds(60);
    dayEntry.setTrack(t);

    when(scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(any(), any()))
        .thenReturn(List.of(dayEntry));

    when(scheduleEntryRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    service.deleteSlotAndRebuildDay(1L);

    verify(scheduleEntryRepository).delete(entry);
    verify(scheduleEntryRepository).saveAll(anyList());
    verify(eventPublisher).publishEvent(any(ScheduleUpdatedEvent.class));
  }

  // ----------------------------------------------------------------------
  // insertTrackIntoDay
  // ----------------------------------------------------------------------

  @Test
  void insertTrackIntoDay_shouldInsertAndRebuild() {
    LocalDate date = LocalDate.of(2025, 1, 1);
    long trackId = 10L;

    Track newTrack = new Track();
    newTrack.setId(trackId);
    newTrack.setDurationSeconds(120);

    when(trackRepository.findById(trackId)).thenReturn(Optional.of(newTrack));

    // existing entry for that day
    ScheduleEntry existing = new ScheduleEntry();
    existing.setId(1L);
    Track existingTrack = new Track();
    existingTrack.setId(2L);
    existingTrack.setDurationSeconds(60);
    existing.setTrack(existingTrack);

    when(scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(any(), any()))
        .thenReturn(new ArrayList<>(List.of(existing)));

    when(scheduleEntryRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(null).build();
    when(scheduleEntryMapper.toDto(any(ScheduleEntry.class))).thenReturn(dto);

    ScheduleEntryDto result = service.insertTrackIntoDay(date, trackId, 0);

    assertNotNull(result);
    verify(scheduleEntryRepository).saveAll(anyList());
    verify(eventPublisher).publishEvent(any(ScheduleUpdatedEvent.class));
  }

  // ----------------------------------------------------------------------
  // changeTrackInSlot
  // ----------------------------------------------------------------------

  @Test
  void changeTrackInSlot_shouldUpdateTrackAndRebuild() {
    long slotId = 1L;
    long newTrackId = 99L;

    ZoneId zone = radioTimeConfig.getRadioZoneId();
    OffsetDateTime start = LocalDate.of(2025, 1, 1).atStartOfDay(zone).toOffsetDateTime();

    ScheduleEntry entry = new ScheduleEntry();
    entry.setId(slotId);
    entry.setStartTime(start);
    entry.setEndTime(start.plusMinutes(5));

    Track oldTrack = new Track();
    oldTrack.setId(1L);
    oldTrack.setDurationSeconds(60);
    entry.setTrack(oldTrack);

    Track newTrack = new Track();
    newTrack.setId(newTrackId);
    newTrack.setDurationSeconds(120);

    when(scheduleEntryRepository.findById(slotId)).thenReturn(Optional.of(entry));
    when(trackRepository.findById(newTrackId)).thenReturn(Optional.of(newTrack));

    // day entries for rebuild
    ScheduleEntry dayEntry = new ScheduleEntry();
    dayEntry.setId(slotId);
    dayEntry.setTrack(newTrack);

    when(scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(any(), any()))
        .thenReturn(List.of(dayEntry));

    when(scheduleEntryRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    ScheduleEntryDto dto = ScheduleEntryDto.builder().id(slotId).build();
    when(scheduleEntryMapper.toDto(entry)).thenReturn(dto);

    ScheduleEntryDto result = service.changeTrackInSlot(slotId, newTrackId);

    assertEquals(slotId, result.getId());
    assertEquals(newTrack, entry.getTrack());
    verify(scheduleEntryRepository).saveAll(anyList());
    verify(eventPublisher).publishEvent(any(ScheduleUpdatedEvent.class));
  }

  // ----------------------------------------------------------------------
  // reorderDay
  // ----------------------------------------------------------------------

  @SuppressWarnings("unchecked")
  @Test
  void reorderDay_shouldReorderAccordingToProvidedIds() {
    LocalDate date = LocalDate.of(2025, 1, 1);

    // three entries in original order: e1, e2, e3
    ScheduleEntry e1 = new ScheduleEntry();
    e1.setId(1L);
    e1.setTrack(track(1L, 60));

    ScheduleEntry e2 = new ScheduleEntry();
    e2.setId(2L);
    e2.setTrack(track(2L, 60));

    ScheduleEntry e3 = new ScheduleEntry();
    e3.setId(3L);
    e3.setTrack(track(3L, 60));

    when(scheduleEntryRepository.findByStartTimeBetweenOrderByStartTime(any(), any()))
        .thenReturn(List.of(e1, e2, e3));

    when(scheduleEntryRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    // desired order: 2, 3, (1 appended automatically at the end)
    service.reorderDay(date, List.of(2L, 3L));

    ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
    verify(scheduleEntryRepository).saveAll(captor.capture());

    List<ScheduleEntry> saved = captor.getValue();
    assertEquals(3, saved.size());
    assertEquals(2L, saved.get(0).getId());
    assertEquals(3L, saved.get(1).getId());
    assertEquals(1L, saved.get(2).getId());

    verify(eventPublisher).publishEvent(any(ScheduleUpdatedEvent.class));
  }

  // ----------------------------------------------------------------------
  // appendPlaylistToTail
  // ----------------------------------------------------------------------

  @Test
  void appendPlaylistToTail_shouldCreateEntriesForAllTracks() {
    long playlistId = 10L;

    Playlist playlist = new Playlist();
    playlist.setId(playlistId);

    when(playlistRepository.findById(playlistId)).thenReturn(Optional.of(playlist));

    Track track1 = track(1L, 60);
    Track track2 = track(2L, 90);

    PlaylistTrack pt1 = new PlaylistTrack();
    pt1.setPlaylist(playlist);
    pt1.setTrack(track1);

    PlaylistTrack pt2 = new PlaylistTrack();
    pt2.setPlaylist(playlist);
    pt2.setTrack(track2);

    when(playlistTrackRepository.findByPlaylistIdOrderByPositionAsc(playlistId))
        .thenReturn(List.of(pt1, pt2));

    when(scheduleEntryRepository.findMaxEndTime()).thenReturn(null);

    when(scheduleEntryRepository.save(any(ScheduleEntry.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    // map entity -> DTO with id = track.id, playlistId = playlist.id
    when(scheduleEntryMapper.toDto(any(ScheduleEntry.class)))
        .thenAnswer(
            inv -> {
              ScheduleEntry e = inv.getArgument(0);
              return ScheduleEntryDto.builder()
                  .id(e.getTrack().getId())
                  .playlistId(e.getPlaylist().getId())
                  .startTime(e.getStartTime().toInstant())
                  .endTime(e.getEndTime().toInstant())
                  .build();
            });

    List<ScheduleEntryDto> result = service.appendPlaylistToTail(playlistId);

    assertEquals(2, result.size());
    assertEquals(playlistId, result.get(0).getPlaylistId());
    assertEquals(playlistId, result.get(1).getPlaylistId());
    assertEquals(1L, result.get(0).getId());
    assertEquals(2L, result.get(1).getId());

    verify(scheduleEntryRepository, times(2)).save(any(ScheduleEntry.class));
    verify(eventPublisher).publishEvent(any(ScheduleUpdatedEvent.class));
  }

  @Test
  void appendPlaylistToTail_shouldThrow_whenPlaylistNotFound() {
    when(playlistRepository.findById(1L)).thenReturn(Optional.empty());
    assertThrows(NotFoundException.class, () -> service.appendPlaylistToTail(1L));
  }

  // ----------------------------------------------------------------------
  // helpers
  // ----------------------------------------------------------------------

  private Track track(Long id, int durationSeconds) {
    Track t = new Track();
    t.setId(id);
    t.setDurationSeconds(durationSeconds);
    return t;
  }
}
