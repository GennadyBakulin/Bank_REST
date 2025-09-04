package com.example.bankcards.security.impl;

import com.example.bankcards.dto.authentification.AuthenticationDtoRequest;
import com.example.bankcards.dto.authentification.JwtDtoResponse;
import com.example.bankcards.dto.authentification.RegistrationDtoRequest;
import com.example.bankcards.entity.user.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.PasswordInvalidException;
import com.example.bankcards.exception.exceptions.UserAlreadyExistException;
import com.example.bankcards.exception.exceptions.UserNotAuthorizeException;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
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

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;

    private final JwtServiceImpl jwtService;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    @Override
    public void register(RegistrationDtoRequest request) {
        checkUniqueEmail(request.getEmail());

        if (!UserUtils.isValidPassword(request.getPassword())) {
            throw new PasswordInvalidException("Invalid password");
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

    @Override
    public JwtDtoResponse refreshToken(HttpServletRequest request, HttpServletResponse response) {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new UserNotAuthorizeException("The user is not logged in");
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

        throw new UserNotAuthorizeException("The user is not logged in");
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        User user = findUserByEmail(username);

        jwtService.revokeAllToken(user);

        clearAuthCookies(response);
    }

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

    private void checkUniqueEmail(@NotBlank @Email String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserAlreadyExistException("User with email " + email + " already exists");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email= " + email + " not found"));
    }
}
