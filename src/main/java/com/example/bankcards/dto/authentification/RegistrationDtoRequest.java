package com.example.bankcards.dto.authentification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistrationDtoRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 1, max = 255)
    private String name;

    @JsonProperty("last-name")
    @NotBlank
    @Size(min = 1, max = 255)
    private String lastName;

    @NotBlank
    @Size(max = 255)
    private String password;
}
