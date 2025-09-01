package com.example.bankcards.dto.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferDtoRequest {

    @JsonProperty("from_card_number")
    @NotBlank
    private String fromCardNumber;

    @JsonProperty("to_card_number")
    @NotBlank
    private String toCardNumber;

    @NotBlank
    @DecimalMin(value = "0.01", message = "Сумма должна быть не менее 0.01")
    private BigDecimal amount;
}
