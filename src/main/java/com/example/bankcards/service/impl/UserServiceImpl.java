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

/**
 * Реализация сервиса для управления пользователями.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * Получает информацию о пользователе по email.
     * Метод доступен только для администраторов.
     *
     * @param email email пользователя для поиска
     * @return UserDtoResponse с информацией о пользователе
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если пользователь с указанным email не найден
     */
    @Override
    public UserDtoResponse getUserByEmail(String email) {
        return mapperToDto(findUserByEmail(email));
    }

    /**
     * Получает список всех пользователей с пагинацией.
     * Метод доступен только для администраторов.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество пользователей на странице
     * @return PageDtoResponse<UserDtoResponse> страница с пользователями
     */
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

    /**
     * Удаляет пользователя по email.
     * Метод доступен только для администраторов.
     *
     * @param email email пользователя для удаления
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если пользователь с указанным email не найден
     */
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

    /**
     * Преобразует сущность User в DTO объект UserDtoResponse.
     *
     * @param user сущность пользователя
     * @return UserDtoResponse с данными пользователя
     */
    private UserDtoResponse mapperToDto(User user) {
        return UserDtoResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .build();
    }
}
