package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.TrackDto;

import java.util.List;

public interface TrackService {

    TrackDto create(TrackDto dto);

    TrackDto getById(Long id);

    List<TrackDto> getAll();

    TrackDto update(Long id, TrackDto dto);

    void delete(Long id);



}
