package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.UserDtoResponse;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDtoResponse getUserByEmail(String email) {
        return mapperToDto(findUserByEmail(email));
    }

    @Override
    public List<UserDtoResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapperToDto).toList();
    }

    @Override
    @Transactional
    public void deleteUserByEmail(String email) {
        userRepository.delete(findUserByEmail(email));
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email= %s not found".formatted(email)));
    }

    private UserDtoResponse mapperToDto(User user) {
        return UserDtoResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
