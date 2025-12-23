package com.Tsimur.Dubcast.service.impl;


import com.Tsimur.Dubcast.dto.UserDto;
import com.Tsimur.Dubcast.exception.type.NotFoundException;
import com.Tsimur.Dubcast.mapper.UserMapper;
import com.Tsimur.Dubcast.model.User;
import com.Tsimur.Dubcast.repository.UserRepository;
import com.Tsimur.Dubcast.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDto create(UserDto dto, String rawPassword) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists");
        }

        dto.setId(null);

        User entity = userMapper.toEntity(dto);
        entity.setPassword(passwordEncoder.encode(rawPassword));



        User saved = userRepository.save(entity);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Override
    public UserDto update(UUID id, UserDto dto) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));

        if (dto.getEmail() != null
                && !dto.getEmail().equals(existing.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("User with email " + dto.getEmail() + " already exists");
        }

        userMapper.updateEntityFromDto(dto, existing);

        User saved = userRepository.save(existing);
        return userMapper.toDto(saved);
    }

    @Override
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw NotFoundException.of("User", "id", id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public void changePassword(UUID id, String rawPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", "id", id));

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}
