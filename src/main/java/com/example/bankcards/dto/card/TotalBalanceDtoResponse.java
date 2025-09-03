package com.example.bankcards.dto.card;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@Schema(description = "Ответ с общим балансом пользователя")
public class TotalBalanceDtoResponse {

    @Schema(description = "Email пользователя", example = "user@example.com")
    private String email;

    @Schema(description = "Общий баланс по всем активным картам", example = "1500.50")
    private BigDecimal totalBalance;
}
