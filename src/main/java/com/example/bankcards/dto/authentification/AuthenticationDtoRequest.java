package com.example.bankcards.dto.authentification;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationDtoRequest {

    @NotBlank
    private String email;

    @NotBlank
    private String password;
}
