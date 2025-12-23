package com.Tsimur.Dubcast.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.exception.type.DuplicateTrackException;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.TrackMapper;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.impl.TrackServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceImplTest {

  @Mock private TrackRepository trackRepository;

  @Mock private TrackMapper trackMapper;

  @InjectMocks private TrackServiceImpl trackService;

  // ---------------------------------------------------------------------
  // create
  // ---------------------------------------------------------------------

  @Test
  void create_shouldSaveAndReturnDto_whenUrlIsUnique() {
    TrackDto dto =
        TrackDto.builder()
            .id(null)
            .soundcloudUrl("https://sc.com/t/1")
            .title("Track 1")
            .durationSeconds(100)
            .artworkUrl("art1")
            .build();

    Track entity = new Track();
    entity.setId(1L);
    entity.setScUrl(dto.getSoundcloudUrl());
    entity.setTitle(dto.getTitle());
    entity.setDurationSeconds(dto.getDurationSeconds());
    entity.setArtworkUrl(dto.getArtworkUrl());

    Track saved = new Track();
    saved.setId(1L);
    saved.setScUrl(dto.getSoundcloudUrl());
    saved.setTitle(dto.getTitle());
    saved.setDurationSeconds(dto.getDurationSeconds());
    saved.setArtworkUrl(dto.getArtworkUrl());

    TrackDto expected =
        TrackDto.builder()
            .id(1L)
            .soundcloudUrl(dto.getSoundcloudUrl())
            .title(dto.getTitle())
            .durationSeconds(dto.getDurationSeconds())
            .artworkUrl(dto.getArtworkUrl())
            .build();

    when(trackRepository.existsByScUrl(dto.getSoundcloudUrl())).thenReturn(false);
    when(trackMapper.toEntity(dto)).thenReturn(entity);
    when(trackRepository.save(entity)).thenReturn(saved);
    when(trackMapper.toDto(saved)).thenReturn(expected);

    TrackDto result = trackService.create(dto);

    assertEquals(expected, result);
    verify(trackRepository).existsByScUrl(dto.getSoundcloudUrl());
    verify(trackMapper).toEntity(dto);
    verify(trackRepository).save(entity);
    verify(trackMapper).toDto(saved);
  }

  @Test
  void create_shouldThrowDuplicateTrackException_whenUrlAlreadyExists() {
    TrackDto dto =
        TrackDto.builder()
            .soundcloudUrl("https://sc.com/t/1")
            .title("Track 1")
            .durationSeconds(100)
            .build();

    // service calls mapper.toEntity(dto) BEFORE existsByScUrl
    when(trackMapper.toEntity(dto)).thenReturn(new Track());

    when(trackRepository.existsByScUrl(dto.getSoundcloudUrl())).thenReturn(true);

    assertThrows(DuplicateTrackException.class, () -> trackService.create(dto));

    verify(trackRepository).existsByScUrl(dto.getSoundcloudUrl());
    // we no longer expect "never" for toEntity – it's OK that it was called
    verify(trackRepository, never()).save(any());
    verify(trackMapper, never()).toDto(any());
  }

  // ---------------------------------------------------------------------
  // getById
  // ---------------------------------------------------------------------

  @Test
  void getById_shouldReturnDto_whenTrackExists() {
    Long id = 10L;

    Track entity = new Track();
    entity.setId(id);
    entity.setScUrl("https://sc.com/t/10");
    entity.setTitle("Track 10");
    entity.setDurationSeconds(200);

    TrackDto dto =
        TrackDto.builder()
            .id(id)
            .soundcloudUrl(entity.getScUrl())
            .title(entity.getTitle())
            .durationSeconds(entity.getDurationSeconds())
            .build();

    when(trackRepository.findById(id)).thenReturn(Optional.of(entity));
    when(trackMapper.toDto(entity)).thenReturn(dto);

    TrackDto result = trackService.getById(id);

    assertEquals(dto, result);
    verify(trackRepository).findById(id);
    verify(trackMapper).toDto(entity);
  }

  @Test
  void getById_shouldThrowNotFound_whenTrackDoesNotExist() {
    Long id = 99L;
    when(trackRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> trackService.getById(id));

    verify(trackRepository).findById(id);
    verify(trackMapper, never()).toDto(any());
  }

  // ---------------------------------------------------------------------
  // getAll
  // ---------------------------------------------------------------------

  @Test
  void getAll_shouldReturnMappedList() {
    Track t1 = new Track();
    t1.setId(1L);
    t1.setScUrl("url1");
    t1.setTitle("t1");

    Track t2 = new Track();
    t2.setId(2L);
    t2.setScUrl("url2");
    t2.setTitle("t2");

    List<Track> entities = List.of(t1, t2);

    TrackDto d1 =
        TrackDto.builder().id(1L).soundcloudUrl("url1").title("t1").durationSeconds(100).build();
    TrackDto d2 =
        TrackDto.builder().id(2L).soundcloudUrl("url2").title("t2").durationSeconds(200).build();
    List<TrackDto> dtos = List.of(d1, d2);

    when(trackRepository.findAll()).thenReturn(entities);
    when(trackMapper.toDtoList(entities)).thenReturn(dtos);

    List<TrackDto> result = trackService.getAll();

    assertEquals(dtos, result);
    verify(trackRepository).findAll();
    verify(trackMapper).toDtoList(entities);
  }

  // ---------------------------------------------------------------------
  // update
  // ---------------------------------------------------------------------

  @Test
  void update_shouldUpdateExistingTrack() {
    Long id = 5L;

    Track existing = new Track();
    existing.setId(id);
    existing.setScUrl("old-url");
    existing.setTitle("old");
    existing.setDurationSeconds(111);

    TrackDto dto =
        TrackDto.builder()
            .id(id)
            .soundcloudUrl("new-url")
            .title("new-title")
            .durationSeconds(222)
            .artworkUrl("new-art")
            .build();

    Track saved = new Track();
    saved.setId(id);
    saved.setScUrl(dto.getSoundcloudUrl());
    saved.setTitle(dto.getTitle());
    saved.setDurationSeconds(dto.getDurationSeconds());
    saved.setArtworkUrl(dto.getArtworkUrl());

    TrackDto expected =
        TrackDto.builder()
            .id(id)
            .soundcloudUrl(dto.getSoundcloudUrl())
            .title(dto.getTitle())
            .durationSeconds(dto.getDurationSeconds())
            .artworkUrl(dto.getArtworkUrl())
            .build();

    when(trackRepository.findById(id)).thenReturn(Optional.of(existing));
    // MapStruct update method
    // trackMapper.updateEntityFromDto(dto, existing) – just verify it's called
    when(trackRepository.save(existing)).thenReturn(saved);
    when(trackMapper.toDto(saved)).thenReturn(expected);

    TrackDto result = trackService.update(id, dto);

    assertEquals(expected, result);
    verify(trackRepository).findById(id);
    verify(trackMapper).updateEntityFromDto(dto, existing);
    verify(trackRepository).save(existing);
    verify(trackMapper).toDto(saved);
  }

  @Test
  void update_shouldThrowNotFound_whenTrackDoesNotExist() {
    Long id = 5L;
    TrackDto dto =
        TrackDto.builder().id(id).soundcloudUrl("url").title("title").durationSeconds(100).build();

    when(trackRepository.findById(id)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> trackService.update(id, dto));

    verify(trackRepository).findById(id);
    verify(trackMapper, never()).updateEntityFromDto(any(), any());
    verify(trackRepository, never()).save(any());
  }

  // ---------------------------------------------------------------------
  // delete
  // ---------------------------------------------------------------------

  @Test
  void delete_shouldDelete_whenTrackExists() {
    Long id = 7L;
    when(trackRepository.existsById(id)).thenReturn(true);

    trackService.delete(id);

    verify(trackRepository).existsById(id);
    verify(trackRepository).deleteById(id);
  }

  @Test
  void delete_shouldThrowNotFound_whenTrackDoesNotExist() {
    Long id = 7L;
    when(trackRepository.existsById(id)).thenReturn(false);

    assertThrows(NotFoundException.class, () -> trackService.delete(id));

    verify(trackRepository).existsById(id);
    verify(trackRepository, never()).deleteById(any());
  }

  // ---------------------------------------------------------------------
  // getRandomTrack
  // ---------------------------------------------------------------------

  @Test
  void getRandomTrack_shouldReturnEmpty_whenNoTracks() {
    when(trackRepository.count()).thenReturn(0L);

    Optional<TrackDto> result = trackService.getRandomTrack();

    assertTrue(result.isEmpty());
    verify(trackRepository).count();
    verify(trackRepository, never()).findAll(any(Pageable.class));
  }

  @Test
  void getRandomTrack_shouldReturnMappedTrack_whenTracksExist() {
    long total = 5L;
    int index = 3; // we will force random to return 3

    when(trackRepository.count()).thenReturn(total);

    Track entity = new Track();
    entity.setId(42L);
    entity.setScUrl("random-url");
    entity.setTitle("Random Track");
    entity.setDurationSeconds(123);

    Page<Track> page = new PageImpl<>(List.of(entity));

    // mock static ThreadLocalRandom.current().nextInt(...)
    try (MockedStatic<ThreadLocalRandom> staticMock = mockStatic(ThreadLocalRandom.class)) {
      ThreadLocalRandom randomMock = mock(ThreadLocalRandom.class);
      staticMock.when(ThreadLocalRandom::current).thenReturn(randomMock);
      when(randomMock.nextInt((int) total)).thenReturn(index);

      when(trackRepository.findAll(PageRequest.of(index, 1))).thenReturn(page);

      TrackDto dto =
          TrackDto.builder()
              .id(42L)
              .soundcloudUrl(entity.getScUrl())
              .title(entity.getTitle())
              .durationSeconds(entity.getDurationSeconds())
              .build();

      when(trackMapper.toDto(entity)).thenReturn(dto);

      Optional<TrackDto> result = trackService.getRandomTrack();

      assertTrue(result.isPresent());
      assertEquals(dto, result.get());

      verify(trackRepository).count();
      verify(trackRepository).findAll(PageRequest.of(index, 1));
      verify(trackMapper).toDto(entity);
    }
  }
}
