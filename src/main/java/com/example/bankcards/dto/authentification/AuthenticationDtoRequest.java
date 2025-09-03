package com.example.bankcards.dto.authentification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на аутентификацию пользователя")
public class AuthenticationDtoRequest {

    @NotBlank
    @Email
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @NotBlank
    @Schema(description = "Пароль пользователя", example = "SecurePassword123!")
    private String password;
}
