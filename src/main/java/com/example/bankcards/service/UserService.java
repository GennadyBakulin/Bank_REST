package com.example.bankcards.service;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;

public interface UserService {

    /**
     * Администратор получает пользователя по email
     */
    UserDtoResponse getUserByEmail(String email);

    /**
     * Администратор получает всех пользователей с пагинацией
     */
    PageDtoResponse<UserDtoResponse> getAllUsers(int pageNumber, int pageSize);

    /**
     * Администратор удаляет пользователя по email
     */
    void deleteByEmail(String email);
}
