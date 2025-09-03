package com.example.bankcards.dto.transfer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Ответ с информацией о переводе")
public class TransferDtoResponse {

    @Schema(description = "Email пользователя, выполнившего перевод", example = "user@example.com")
    private String userEmail;

    @Schema(description = "Номер карты с которой произведен перевод средств", example = "1234567812345678")
    private String fromCardNumber;

    @Schema(description = "Номер карты на которую произведен перевод средств", example = "1234567812345678")
    private String toCardNumber;

    @Schema(description = "Сумма перевода", example = "1000.00")
    private BigDecimal amount;

    @Schema(description = "Время выполнения перевода", example = "2024-01-15T14:30:00")
    private LocalDateTime time;
}
