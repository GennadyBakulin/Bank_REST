package com.example.bankcards.service;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;

/**
 * Сервис для управления пользователями.
 */
public interface UserService {

    /**
     * Получает информацию о пользователе по email.
     * Метод доступен только для администраторов.
     *
     * @param email email пользователя для поиска
     * @return UserDtoResponse с информацией о пользователе
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если пользователь с указанным email не найден
     */
    UserDtoResponse getUserByEmail(String email);

    /**
     * Получает список всех пользователей с пагинацией.
     * Метод доступен только для администраторов.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество пользователей на странице
     * @return PageDtoResponse<UserDtoResponse> страница с пользователями
     */
    PageDtoResponse<UserDtoResponse> getAllUsers(int pageNumber, int pageSize);

    /**
     * Удаляет пользователя по email.
     * Метод доступен только для администраторов.
     *
     * @param email email пользователя для удаления
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если пользователь с указанным email не найден
     */
    void deleteByEmail(String email);
}
