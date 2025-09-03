package com.example.bankcards.controller;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.user.UserDtoResponse;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями системы")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить пользователя по email",
            description = "Администратор получает информацию о пользователе по email",
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "Email пользователя",
                            required = true,
                            example = "user@example.com"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно найден",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "name": "John",
                                              "lastName": "Doe",
                                              "role": "USER"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<UserDtoResponse> getUserByEmail(@RequestParam String email) {

        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить всех пользователей",
            description = "Администратор получает список всех пользователей с пагинацией",
            parameters = {
                    @Parameter(
                            name = "page",
                            description = "Номер страницы (начиная с 0)",
                            example = "0"
                    ),
                    @Parameter(
                            name = "size",
                            description = "Размер страницы",
                            example = "10"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Список пользователей успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "email": "user1@example.com",
                                                  "name": "John",
                                                  "lastName": "Doe",
                                                  "role": "USER"
                                                },
                                                {
                                                  "email": "admin@example.com",
                                                  "name": "Jane",
                                                  "lastName": "Smith",
                                                  "role": "ADMIN"
                                                }
                                              ],
                                              "totalElements": 2,
                                              "totalPages": 1,
                                              "currentPage": 0
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<PageDtoResponse<UserDtoResponse>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удалить пользователя",
            description = "Администратор удаляет пользователя по email",
            parameters = {
                    @Parameter(
                            name = "email",
                            description = "Email пользователя для удаления",
                            required = true,
                            example = "user@example.com"
                    )
            }
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Пользователь успешно удален",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "The user has been successfully deleted")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<String> deleteUser(
            @RequestParam String email) {

        userService.deleteByEmail(email);
        return ResponseEntity.ok("The user has been successfully deleted");
    }
}
