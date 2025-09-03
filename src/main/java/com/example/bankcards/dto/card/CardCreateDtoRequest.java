package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на создание новой карты")
public class CardCreateDtoRequest {

    @JsonProperty("card_number")
    @NotBlank
    @Size(min = 16, max = 16, message = "Номер карты должен состоять из 16 цифр")
    @Schema(description = "Номер карты", example = "1234567812345678")
    private String number;

    @JsonProperty("user_email")
    @NotBlank
    @Email
    @Schema(description = "Email пользователя-владельца карты", example = "user@example.com")
    private String userEmail;

    @JsonProperty("count_month")
    @Min(value = 1, message = "Количество месяцев должно быть не менее 1")
    @Schema(description = "Срок действия карты в месяцах", example = "36")
    private int countMonth;

    @DecimalMin(value = "0.00", message = "Сумма не может быть отрицательной")
    @Schema(description = "Начальный баланс карты", example = "1000.00")
    private BigDecimal amount;
}
