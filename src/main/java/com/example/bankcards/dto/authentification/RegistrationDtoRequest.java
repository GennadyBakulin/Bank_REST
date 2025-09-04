package com.example.bankcards.dto.authentification;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на регистрацию пользователя")
public class RegistrationDtoRequest {

    @NotBlank
    @Email
    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 1, max = 125)
    @Schema(description = "Имя пользователя", example = "John")
    private String name;

    @JsonProperty("last_name")
    @NotBlank
    @Size(min = 1, max = 125)
    @Schema(description = "Фамилия пользователя", example = "Doe")
    private String lastName;

    @NotBlank
    @Size(min = 5, max = 16, message = "Пароль должен иметь длину от 5 до 16 символов " +
            "и состоять из цифр от 0 до 9 и символов от a до z и A до Z")
    @Schema(description = "Пароль пользователя", example = "SecurePassword123!")
    private String password;
}
