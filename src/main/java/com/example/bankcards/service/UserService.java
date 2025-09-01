package com.example.bankcards.service;

import com.example.bankcards.dto.user.UserDtoResponse;

import java.util.List;

public interface UserService {

    /**
     * Находит пользователя по email
     */
    UserDtoResponse getUserByEmail(String email);

    /**
     * Получает всех пользователей без пагинации!!!
     */
    List<UserDtoResponse> getAllUsers();

    /**
     * Удаляет пользователя по email
     */
    void deleteUserByEmail(String email);
}
