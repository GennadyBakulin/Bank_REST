package com.example.bankcards.service;

import com.example.bankcards.dto.authentification.JwtDtoResponse;
import com.example.bankcards.dto.authentification.AuthorizationDtoRequest;
import com.example.bankcards.dto.authentification.RegistrationDtoRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    /**
     * Регистрирует нового пользователя
     */
    void register(RegistrationDtoRequest request);

    /**
     * Авторизация пользователя
     */
    JwtDtoResponse authenticate(AuthorizationDtoRequest request);

    ResponseEntity<JwtDtoResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response);
}
