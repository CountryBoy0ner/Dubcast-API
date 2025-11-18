package com.Tsimur.Dubcast.mapper;


import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.dto.UserDto;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = { DateTimeMapper.class })
public interface UserMapper {

    UserDto toDto(User entity);

    @Mapping(target = "password", ignore = true)
    User toEntity(UserDto dto);

    List<UserDto> toDtoList(List<User> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(UserDto dto, @MappingTarget User entity);
}
