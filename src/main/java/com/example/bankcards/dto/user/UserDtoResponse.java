package com.example.bankcards.dto.user;

import com.example.bankcards.entity.user.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDtoResponse {

    private String email;

    private String name;

    private String lastName;

    private Role role;
}
