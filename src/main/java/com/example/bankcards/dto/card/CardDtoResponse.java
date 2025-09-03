package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@Schema(description = "Ответ с информацией о карте")
public class CardDtoResponse {

    @Schema(description = "Замаскированный номер карты", example = "**** **** **** 5678")
    private String maskedCardNumber;

    @Schema(description = "Email владельца карты", example = "user@example.com")
    private String email;

    @Schema(description = "Полное имя владельца карты", example = "John Doe")
    private String fullNameUser;

    @Schema(description = "Дата окончания действия карты", example = "2027-01-15")
    private LocalDate expirationDate;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс карты", example = "1000.00")
    private BigDecimal balance;
}
