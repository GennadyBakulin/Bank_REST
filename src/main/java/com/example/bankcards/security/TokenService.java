package com.example.bankcards.security;

import com.example.bankcards.entity.user.User;

public interface TokenService {

    void revokeAllToken(User user);

    void saveUserToken(String accessToken, String refreshToken, User user);
}
