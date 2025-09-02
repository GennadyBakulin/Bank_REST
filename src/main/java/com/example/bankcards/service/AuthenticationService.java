package com.example.bankcards.service;

import com.example.bankcards.dto.authentification.AuthenticationDtoRequest;
import com.example.bankcards.dto.authentification.JwtDtoResponse;
import com.example.bankcards.dto.authentification.RegistrationDtoRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface AuthenticationService {

    /**
     * Регистрация нового пользователя
     */
    void register(RegistrationDtoRequest request);

    /**
     * Аутентификация пользователя
     */
    JwtDtoResponse authenticate(AuthenticationDtoRequest request);

    ResponseEntity<JwtDtoResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response);
}
