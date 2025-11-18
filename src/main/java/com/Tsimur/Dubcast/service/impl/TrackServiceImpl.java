package com.Tsimur.Dubcast.service.impl;


import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.mapper.TrackMapper;
import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.repository.TrackRepository;
import com.Tsimur.Dubcast.service.TrackService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final TrackMapper trackMapper;

    @Override
    public TrackDto create(TrackDto dto) {
        Track entity = trackMapper.toEntity(dto);
        entity.setId(null);
        Track saved = trackRepository.save(entity);
        return trackMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TrackDto getById(Long id) {
        Track track = trackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Track not found: " + id));
        return trackMapper.toDto(track);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrackDto> getAll() {
        return trackMapper.toDtoList(trackRepository.findAll());
    }

    @Override
    public TrackDto update(Long id, TrackDto dto) {
        Track existing = trackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Track not found: " + id));

        trackMapper.updateEntityFromDto(dto, existing);

        Track saved = trackRepository.save(existing);
        return trackMapper.toDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!trackRepository.existsById(id)) {
            throw new EntityNotFoundException("Track not found: " + id);
        }
        trackRepository.deleteById(id);
    }
}
