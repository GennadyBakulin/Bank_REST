package com.example.bankcards.service;

import com.example.bankcards.dto.authentification.AuthenticationDtoRequest;
import com.example.bankcards.dto.authentification.JwtDtoResponse;
import com.example.bankcards.dto.authentification.RegistrationDtoRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Сервис аутентификации и управления пользовательскими сессиями.
 * Предоставляет методы для регистрации, входа, обновления токенов и выхода из системы
 */
public interface AuthenticationService {

    /**
     * Регистрирует нового пользователя в системе.
     * Создает учетную запись пользователя с предоставленными данными
     *
     * @param request объект с данными для регистрации
     */
    void register(RegistrationDtoRequest request);

    /**
     * Аутентифицирует пользователя в системе.
     * Проверяет учетные данные и возвращает JWT токены при успешной аутентификации
     *
     * @param request объект с данными для аутентификации, содержащий логин и пароль
     * @return объект JwtDtoResponse с access и refresh токенами
     */
    JwtDtoResponse authenticate(AuthenticationDtoRequest request);

    /**
     * Обновляет пару JWT токенов (access и refresh) на основе валидного refresh токена.
     *
     * @param request  HTTP запрос, содержащий refresh токен
     * @param response HTTP ответ, в который могут быть установлены обновленные токены
     * @return объект JwtDtoResponse с новыми access и refresh токенами
     */
    JwtDtoResponse refreshToken(
            HttpServletRequest request,
            HttpServletResponse response);

    /**
     * Выполняет выход пользователя из системы.
     * Отзывает текущие JWT токены пользователя и очищает данные сессии.
     *
     * @param request  HTTP запрос, содержащий токены для отзыва
     * @param response HTTP ответ, в котором очищаются токены (например, удаляются cookies)
     */
    void logout(HttpServletRequest request, HttpServletResponse response);
}
