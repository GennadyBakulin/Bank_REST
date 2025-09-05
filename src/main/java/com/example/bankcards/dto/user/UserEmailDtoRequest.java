package com.example.bankcards.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос с email пользователя")
public class UserEmailDtoRequest {

    @NotBlank
    @Email
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;
}
