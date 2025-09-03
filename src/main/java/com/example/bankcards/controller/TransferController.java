package com.example.bankcards.controller;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
@Tag(name = "Transfer Management", description = "API для управления переводами средств между картами")
@SecurityRequirement(name = "Bearer Authentication")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/my/between-cards")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Перевод между своими картами",
            description = "Пользователь осуществляет перевод средств между своими картами",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для перевода",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransferDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "fromCardNumber": "1234567812345678",
                                              "toCardNumber": "8765432187654321",
                                              "amount": 1000.00
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Перевод успешно выполнен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransferDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "userEmail": "user@example.com",
                                              "fromCardNumber": "1234567812345678",
                                              "toCardNumber": "8765432187654321",
                                              "amount": 1000.00,
                                              "time": "2024-01-15T14:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса или недостаточно средств"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Карта не принадлежит пользователю"
            )
    })
    public ResponseEntity<TransferDtoResponse> transferBetweenOwnCards(
            @Valid @RequestBody TransferDtoRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transferService.transferBetweenCardsOneUser(request));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить все переводы (админ)",
            description = "Администратор получает список всех переводов в системе с пагинацией",
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
                    description = "Список переводов успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "userEmail": "user1@example.com",
                                                  "fromCardNumber": "1234567812345678",
                                                  "toCardNumber": "8765432187654321",
                                                  "amount": 1000.00,
                                                  "time": "2024-01-15T14:30:00"
                                                },
                                                {
                                                  "userEmail": "user2@example.com",
                                                  "fromCardNumber": "1111222233334444",
                                                  "toCardNumber": "5555666677778888",
                                                  "amount": 500.50,
                                                  "time": "2024-01-15T15:45:00"
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
    public ResponseEntity<PageDtoResponse<TransferDtoResponse>> getAllTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(transferService.getAll(page, size));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Получить мои переводы",
            description = "Пользователь получает список своих переводов с пагинацией",
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
                    description = "Список переводов пользователя успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "userEmail": "user@example.com",
                                                  "fromCardNumber": "1234567812345678",
                                                  "toCardNumber": "8765432187654321",
                                                  "amount": 1000.00,
                                                  "time": "2024-01-15T14:30:00"
                                                },
                                                {
                                                  "userEmail": "user@example.com",
                                                  "fromCardNumber": "1234567812345678",
                                                  "toCardNumber": "9999888877776666",
                                                  "amount": 250.75,
                                                  "time": "2024-01-14T10:15:00"
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
                    description = "Доступ запрещен. Требуется аутентификация"
            )
    })
    public ResponseEntity<PageDtoResponse<TransferDtoResponse>> getMyTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(transferService.getAllByUser(page, size));
    }
}
