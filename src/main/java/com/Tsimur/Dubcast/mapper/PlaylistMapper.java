package com.Tsimur.Dubcast.mapper;

import com.Tsimur.Dubcast.dto.PlaylistDto;
import com.Tsimur.Dubcast.model.Playlist;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlaylistMapper {

    @Mapping(target = "soundcloudUrl", source = "scPlaylistUrl")
    @Mapping(target = "title",         source = "name")
    @Mapping(target = "totalTracks",   ignore = true)
    PlaylistDto toDto(Playlist entity);

    @InheritInverseConfiguration
    @Mapping(target = "id", ignore = true)
    Playlist toEntity(PlaylistDto dto);
}
