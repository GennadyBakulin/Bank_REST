package com.example.bankcards.security;

import com.example.bankcards.entity.user.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

/**
 * Сервис для работы с JWT (JSON Web Token) токенами.
 * Предоставляет методы для генерации, валидации и управления JWT токенами.
 */
public interface JwtService {

    /**
     * Проверяет валидность access токена для указанного пользователя.
     *
     * @param token JWT токен для проверки
     * @param user  данные пользователя для верификации
     * @return true если токен валиден и соответствует пользователю, иначе false
     */
    boolean isValid(String token, UserDetails user);

    /**
     * Проверяет валидность refresh токена для указанного пользователя.
     *
     * @param token refresh токен для проверки
     * @param user  пользователь для верификации
     * @return true если refresh токен валиден и соответствует пользователю, иначе false
     */
    boolean isValidRefresh(String token, User user);

    /**
     * Извлекает имя пользователя из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя, содержащееся в токене
     */
    String extractUsername(String token);

    /**
     * Извлекает конкретное утверждение (claim) из JWT токена.
     *
     * @param <T>      тип возвращаемого значения
     * @param token    JWT токен
     * @param resolver функция для извлечения и преобразования утверждения
     * @return значение утверждения указанного типа
     */
    <T> T extractClaim(String token, Function<Claims, T> resolver);

    /**
     * Генерирует access токен для указанного пользователя.
     *
     * @param user пользователь, для которого генерируется токен
     * @return сгенерированный JWT access токен
     */
    String generateAccessToken(User user);

    /**
     * Генерирует refresh токен для указанного пользователя.
     *
     * @param user пользователь, для которого генерируется токен
     * @return сгенерированный JWT refresh токен
     */
    String generateRefreshToken(User user);

    /**
     * Отзывает все токены пользователя.
     * Делает все ранее выданные токены пользователя недействительными.
     *
     * @param user пользователь, токены которого нужно отозвать
     */
    void revokeAllToken(User user);

    /**
     * Сохраняет сгенерированные токены для пользователя.
     *
     * @param accessToken  access токен
     * @param refreshToken refresh токен
     * @param user         пользователь, для которого сохраняются токены
     */
    void saveUserToken(String accessToken, String refreshToken, User user);
}
