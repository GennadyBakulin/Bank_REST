package com.example.bankcards.controller;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserDtoResponse> getUserByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/all-users")
    public ResponseEntity<PageDtoResponse<UserDtoResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(
            @RequestParam String email) {

        userService.deleteByEmail(email);
        return ResponseEntity.ok("The user has been successfully deleted");
    }
}
