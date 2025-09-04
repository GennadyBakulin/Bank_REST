package com.example.bankcards.service.impl;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PageDtoResponse<UserDtoResponse> getAllUsers(int pageNumber, int pageSize) {
        Page<User> pageUsers = userRepository.findAll(PageRequest.of(pageNumber, pageSize));

        List<UserDtoResponse> content = pageUsers.getContent().stream()
                .map(this::mapperToDto)
                .toList();

        return new PageDtoResponse<>(
                content,
                pageUsers.getTotalElements(),
                pageUsers.getTotalPages(),
                pageUsers.getNumber());
    }

    @Override
    @Transactional
    public void deleteByEmail(String email) {
        userRepository.delete(findUserByEmail(email));
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email= " + email + " not found"));
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
