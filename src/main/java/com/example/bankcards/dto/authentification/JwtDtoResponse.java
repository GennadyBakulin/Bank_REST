package com.example.bankcards.dto.authentification;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class JwtDtoResponse {

    private final String accessToken;

    private final String refreshToken;
}
