package com.Tsimur.Dubcast.service.impl;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.exception.type.DuplicateTrackException;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.TrackMapper;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.TrackService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackServiceImpl implements TrackService {

  private final TrackRepository trackRepository;
  private final TrackMapper trackMapper;

  @Override
  public TrackDto create(TrackDto dto) {
    Track entity = trackMapper.toEntity(dto);
    if (trackRepository.existsByScUrl(dto.getSoundcloudUrl())) {
      throw new DuplicateTrackException(
          "Track with url: " + dto.getSoundcloudUrl() + " already exists");
    }
    Track saved = trackRepository.save(entity);
    return trackMapper.toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public TrackDto getById(Long id) {
    Track track =
        trackRepository.findById(id).orElseThrow(() -> NotFoundException.of("Track", "id", id));
    return trackMapper.toDto(track);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TrackDto> getAll() {
    return trackMapper.toDtoList(trackRepository.findAll());
  }

  @Override
  public TrackDto update(Long id, TrackDto dto) {
    Track existing =
        trackRepository.findById(id).orElseThrow(() -> NotFoundException.of("Track", "id", id));

    trackMapper.updateEntityFromDto(dto, existing);

    Track saved = trackRepository.save(existing);
    return trackMapper.toDto(saved);
  }

  @Override
  public void delete(Long id) {
    if (!trackRepository.existsById(id)) {
      throw NotFoundException.of("Track", "id", id);
    }
    trackRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<TrackDto> getRandomTrack() {
    long total = trackRepository.count();
    if (total == 0) {
      return Optional.empty();
    }

    int index = ThreadLocalRandom.current().nextInt((int) total);

    Page<Track> page = trackRepository.findAll(PageRequest.of(index, 1));

    return page.stream().findFirst().map(trackMapper::toDto);
  }
}
