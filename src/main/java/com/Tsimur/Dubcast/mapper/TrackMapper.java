package com.Tsimur.Dubcast.mapper;


import com.Tsimur.Dubcast.model.Track;
import com.Tsimur.Dubcast.dto.TrackDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {DateTimeMapper.class}
)
public interface TrackMapper {

    @Mapping(source = "scUrl", target = "soundcloudUrl")
    TrackDto toDto(Track entity);

    @Mapping(source = "soundcloudUrl", target = "scUrl")
    Track toEntity(TrackDto dto);

    List<TrackDto> toDtoList(List<Track> entities);

    List<Track> toEntityList(List<TrackDto> dtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "scUrl", source = "soundcloudUrl")
    void updateEntityFromDto(TrackDto dto, @MappingTarget Track entity);
}
