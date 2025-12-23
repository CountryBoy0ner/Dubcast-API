package com.Tsimur.Dubcast.mapper;

import com.Tsimur.Dubcast.dto.PlaylistTrackDto;
import com.Tsimur.Dubcast.model.PlaylistTrack;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
    componentModel = "spring",
    uses = {TrackMapper.class})
public interface PlaylistTrackMapper {

  @Mapping(source = "track", target = "track")
  PlaylistTrackDto toDto(PlaylistTrack entity);

  @Mapping(target = "playlist", ignore = true)
  @Mapping(source = "track", target = "track")
  PlaylistTrack toEntity(PlaylistTrackDto dto);
}
