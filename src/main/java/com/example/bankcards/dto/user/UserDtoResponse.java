package com.example.bankcards.dto.user;

import com.example.bankcards.entity.user.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Ответ с информацией о пользователе")
public class UserDtoResponse {

    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @Schema(description = "Имя пользователя", example = "John")
    private String name;

    @Schema(description = "Фамилия пользователя", example = "Doe")
    private String lastName;

    @Schema(description = "Роль пользователя в системе", example = "USER")
    private Role role;
}
