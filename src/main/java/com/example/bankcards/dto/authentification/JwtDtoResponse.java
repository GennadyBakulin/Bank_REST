package com.example.bankcards.dto.authentification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Ответ с JWT токенами")
public class JwtDtoResponse {

    @Schema(description = "Access token для авторизации", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String accessToken;

    @Schema(description = "Refresh token для обновления access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String refreshToken;
}
