package com.Tsimur.Dubcast.mapper;

import com.Tsimur.Dubcast.dto.TrackDto;
import com.Tsimur.Dubcast.model.Track;
import java.util.List;
import org.mapstruct.*;

@Mapper(
    componentModel = "spring",
    uses = {DateTimeMapper.class})
public interface TrackMapper {

  @Mapping(source = "scUrl", target = "soundcloudUrl")
  @Mapping(source = "likesCount", target = "likesCount")
  TrackDto toDto(Track entity);

  @Mapping(source = "soundcloudUrl", target = "scUrl")
  @Mapping(target = "likesCount", ignore = true)
  Track toEntity(TrackDto dto);

  List<TrackDto> toDtoList(List<Track> entities);

  List<Track> toEntityList(List<TrackDto> dtos);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "likesCount", ignore = true)
  @Mapping(target = "scUrl", source = "soundcloudUrl")
  void updateEntityFromDto(TrackDto dto, @MappingTarget Track entity);
}
