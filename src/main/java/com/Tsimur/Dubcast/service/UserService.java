package com.Tsimur.Dubcast.service;

import com.Tsimur.Dubcast.dto.UserDto;
import com.Tsimur.Dubcast.dto.response.UserProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserDto create(UserDto dto, String rawPassword);

    UserDto getById(UUID id);

    List<UserDto> getAll();

    UserDto update(UUID id, UserDto dto);

    void delete(UUID id);

    void changePassword(UUID id, String rawPassword);


    UserProfileResponse getCurrentUserProfile();

    void updateCurrentUserBio(String bio);

    void updateCurrentUserUsername(String username);
}
