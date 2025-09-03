package com.example.bankcards.dto.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Запрос на перевод средств между картами")
public class TransferDtoRequest {

    @JsonProperty("from_card_number")
    @NotBlank
    @Size(min = 16, max = 16, message = "Номер карты должен состоять из 16 цифр")
    @Schema(description = "Номер карты с которой произойдет перевод средств", example = "1234567812345678")
    private String fromCardNumber;

    @JsonProperty("to_card_number")
    @NotBlank
    @Size(min = 16, max = 16, message = "Номер карты должен состоять из 16 цифр")
    @Schema(description = "Номер карты на которую переводятся средства", example = "8765432187654321")
    private String toCardNumber;

    @NotBlank
    @DecimalMin(value = "0.01", message = "Сумма должна быть больше 0")
    @Schema(description = "Сумма перевода", example = "1000.00")
    private BigDecimal amount;
}
