package com.example.bankcards.security.impl;

import com.example.bankcards.dto.authentification.AuthenticationDtoRequest;
import com.example.bankcards.dto.authentification.JwtDtoResponse;
import com.example.bankcards.dto.authentification.RegistrationDtoRequest;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.ConflictRequestException;
import com.example.bankcards.exception.exceptions.InvalidRequestException;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
import com.example.bankcards.exception.exceptions.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthenticationService;
import com.example.bankcards.util.UserUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса аутентификации и управления пользовательскими сессиями.
 * Обеспечивает регистрацию, аутентификацию, обновление токенов и выход из системы.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final JwtServiceImpl jwtService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    /**
     * Регистрирует нового пользователя в системе.
     * Проверяет уникальность email и валидность пароля перед созданием учетной записи.
     *
     * @param request объект RegistrationDtoRequest с данными для регистрации
     * @throws ConflictRequestException если пользователь с таким email уже существует
     * @throws InvalidRequestException  если пароль не соответствует требованиям безопасности
     */
    @Override
    public void register(RegistrationDtoRequest request) {
        checkUniqueEmail(request.getEmail());

        if (!UserUtils.isValidPassword(request.getPassword())) {
            throw new InvalidRequestException("Invalid password");
        }

        User user = new User(
                request.getEmail(),
                request.getName(),
                request.getLastName(),
                Role.USER,
                passwordEncoder.encode(request.getPassword())
        );

        userRepository.save(user);
    }

    /**
     * Аутентифицирует пользователя и генерирует JWT токены.
     *
     * @param request объект AuthenticationDtoRequest с email и паролем
     * @return JwtDtoResponse с access и refresh токенами
     * @throws ResourceNotFoundException                                           если пользователь с указанным email не найден
     * @throws org.springframework.security.authentication.BadCredentialsException если неверные учетные данные
     */
    @Override
    public JwtDtoResponse authenticate(AuthenticationDtoRequest request) {
        User user = findUserByEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        jwtService.revokeAllToken(user);

        jwtService.saveUserToken(accessToken, refreshToken, user);

        return JwtDtoResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Обновляет пару JWT токенов на основе валидного refresh токена.
     *
     * @param request  HTTP запрос с refresh токеном в заголовке Authorization
     * @param response HTTP ответ для возможной установки cookies
     * @return JwtDtoResponse с новыми access и refresh токенами
     * @throws UnauthorizedException     если заголовок Authorization отсутствует или невалиден
     * @throws UnauthorizedException     если refresh токен невалиден
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Override
    public JwtDtoResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("The user is not logged in");
        }

        String token = authorizationHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = findUserByEmail(username);

        if (jwtService.isValidRefresh(token, user)) {

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            jwtService.revokeAllToken(user);

            jwtService.saveUserToken(accessToken, refreshToken, user);

            return JwtDtoResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        throw new UnauthorizedException("The user is not logged in");
    }

    /**
     * Выполняет выход пользователя из системы.
     *
     * @param request  HTTP запрос с access токеном в заголовке Authorization
     * @param response HTTP ответ для очистки cookies
     * @throws UnauthorizedException     если заголовок Authorization отсутствует или невалиден
     * @throws ResourceNotFoundException если пользователь не найден
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = findUserByEmail(username);

        jwtService.revokeAllToken(user);

        clearAuthCookies(response);
    }

    /**
     * Очищает authentication cookies в HTTP ответе.
     * Устанавливает cookies accessToken и refreshToken с нулевым временем жизни.
     *
     * @param response HTTP ответ для очистки cookies
     */
    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessCookie = new Cookie("accessToken", null);
        accessCookie.setHttpOnly(true);
        accessCookie.setSecure(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);
    }

    /**
     * Проверяет уникальность email в системе.
     *
     * @param email email для проверки
     * @throws ConflictRequestException если пользователь с таким email уже существует
     */
    private void checkUniqueEmail(@NotBlank @Email String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictRequestException("User with email " + email + " already exists");
        }
    }

    /**
     * Находит пользователя по email.
     *
     * @param email email пользователя
     * @return сущность User
     * @throws ResourceNotFoundException если пользователь с указанным email не найден
     */
    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email= " + email + " not found"));
    }
}
