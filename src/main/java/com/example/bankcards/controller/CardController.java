package com.example.bankcards.controller;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.CardNumberDtoRequest;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.service.CardService;
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
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Card Management", description = "API для управления банковскими картами")
@SecurityRequirement(name = "Bearer Authentication")
public class CardController {

    private final CardService cardService;

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Создать новую карту (админ)",
            description = "Администратор создает новую банковскую карту для пользователя",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для создания карты",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardCreateDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "number": "1234567812345678",
                                              "userEmail": "user@example.com",
                                              "countMonth": 36,
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
                    description = "Карта успешно создана",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "maskedCardNumber": "**** **** **** 5678",
                                              "email": "user@example.com",
                                              "fullNameUser": "John Doe",
                                              "expirationDate": "2027-01-15",
                                              "status": "ACTIVE",
                                              "balance": 1000.00
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Невалидные данные запроса"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Пользователь не найден"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Карта с таким номером уже существует"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<CardDtoResponse> createCard(@Valid @RequestBody CardCreateDtoRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.create(request));
    }

    @PatchMapping("/admin/block")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Заблокировать карту (админ)",
            description = "Администратор блокирует карту по номеру",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты для блокировки",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardNumberDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "number": "1234567812345678"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно заблокирована"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<Void> blockCard(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.blocked(request.getNumber());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Активировать карту (админ)",
            description = "Администратор активирует карту по номеру",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты для активации",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardNumberDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "number": "1234567812345678"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Карта успешно активирована"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Нельзя активировать карту с истекшим сроком"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<Void> activateCard(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.activation(request.getNumber());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Удалить карту (админ)",
            description = "Администратор удаляет карту по номеру",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты для удаления",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardNumberDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "number": "1234567812345678"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Карта успешно удалена"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуются права администратора"
            )
    })
    public ResponseEntity<Void> deleteCard(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.delete(request.getNumber());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Получить все карты (админ)",
            description = "Администратор получает список всех карт в системе с пагинацией",
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
                    description = "Список карт успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "maskedCardNumber": "**** **** **** 5678",
                                                  "email": "user1@example.com",
                                                  "fullNameUser": "John Doe",
                                                  "expirationDate": "2027-01-15",
                                                  "status": "ACTIVE",
                                                  "balance": 1000.00
                                                },
                                                {
                                                  "maskedCardNumber": "**** **** **** 4321",
                                                  "email": "user2@example.com",
                                                  "fullNameUser": "Jane Smith",
                                                  "expirationDate": "2026-05-20",
                                                  "status": "BLOCKED",
                                                  "balance": 500.50
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
    public ResponseEntity<PageDtoResponse<CardDtoResponse>> getAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(cardService.getAll(page, size));
    }

    @GetMapping("/my/all")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Получить мои карты",
            description = "Пользователь получает список всех своих карт с пагинацией",
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
                    description = "Список карт пользователя успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "maskedCardNumber": "**** **** **** 5678",
                                                  "email": "user1@example.com",
                                                  "fullNameUser": "John Doe",
                                                  "expirationDate": "2027-01-15",
                                                  "status": "ACTIVE",
                                                  "balance": 1000.00
                                                },
                                                {
                                                  "maskedCardNumber": "**** **** **** 4321",
                                                  "email": "user2@example.com",
                                                  "fullNameUser": "Jane Smith",
                                                  "expirationDate": "2026-05-20",
                                                  "status": "BLOCKED",
                                                  "balance": 500.50
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
    public ResponseEntity<PageDtoResponse<CardDtoResponse>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(cardService.getAllByUser(page, size));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Получить карту по номеру",
            description = "Пользователь получает информацию о своей карте по номеру",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardNumberDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "number": "1234567812345678"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о карте успешно получена",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "maskedCardNumber": "**** **** **** 5678",
                                              "email": "user@example.com",
                                              "fullNameUser": "John Doe",
                                              "expirationDate": "2027-01-15",
                                              "status": "ACTIVE",
                                              "balance": 1000.00
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена или не принадлежит пользователю"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуется аутентификация"
            )
    })
    public ResponseEntity<CardDtoResponse> getCardByNumber(@Valid @RequestBody CardNumberDtoRequest request) {

        return ResponseEntity.ok(cardService.getByNumber(request.getNumber()));
    }

    @PatchMapping("/my/request-block")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Запрос на блокировку карты",
            description = "Пользователь отправляет запрос на блокировку своей карты",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Номер карты для блокировки",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CardNumberDtoRequest.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "number": "1234567812345678"
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Запрос на блокировку карты успешно отправлен"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Карта не найдена или не принадлежит пользователю"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Доступ запрещен. Требуется аутентификация"
            )
    })
    public ResponseEntity<Void> requestCardBlock(@Valid @RequestBody CardNumberDtoRequest request) {

        cardService.requestToBlocked(request.getNumber());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my/total-balance")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Получить общий баланс",
            description = "Пользователь получает общий баланс со всех своих активных карт"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Общий баланс успешно получен",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TotalBalanceDtoResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "email": "user@example.com",
                                              "totalBalance": 1500.50
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
    public ResponseEntity<TotalBalanceDtoResponse> getTotalBalance() {

        return ResponseEntity.ok(cardService.getTotalBalanceUser());
    }
}
