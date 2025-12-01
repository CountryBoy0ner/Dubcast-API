package com.Tsimur.Dubcast.mapper;

import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import com.Tsimur.Dubcast.model.ScheduleEntry;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {TrackMapper.class, DateTimeMapper.class}
)
public interface ScheduleEntryMapper {

    @Mapping(source = "playlist.id", target = "playlistId")
    ScheduleEntryDto toDto(ScheduleEntry entity);

    @Mapping(source = "playlistId", target = "playlist.id")
    ScheduleEntry toEntity(ScheduleEntryDto dto);

    List<ScheduleEntryDto> toDtoList(List<ScheduleEntry> entities);

    List<ScheduleEntry> toEntityList(List<ScheduleEntryDto> dtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "playlist", ignore = true) // 👈 плейлист не меняем патчем
    void updateEntityFromDto(ScheduleEntryDto dto, @MappingTarget ScheduleEntry entity);
}
