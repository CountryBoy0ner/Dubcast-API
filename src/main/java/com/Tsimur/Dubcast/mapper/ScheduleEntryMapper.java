package com.Tsimur.Dubcast.mapper;


import com.Tsimur.Dubcast.model.ScheduleEntry;
import com.Tsimur.Dubcast.dto.ScheduleEntryDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {TrackMapper.class, DateTimeMapper.class}
)
public interface ScheduleEntryMapper {

    ScheduleEntryDto toDto(ScheduleEntry entity);

    ScheduleEntry toEntity(ScheduleEntryDto dto);

    List<ScheduleEntryDto> toDtoList(List<ScheduleEntry> entities);

    List<ScheduleEntry> toEntityList(List<ScheduleEntryDto> dtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(ScheduleEntryDto dto, @MappingTarget ScheduleEntry entity);
}
