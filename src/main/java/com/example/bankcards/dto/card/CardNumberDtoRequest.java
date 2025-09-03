package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос с номером карты")
public class CardNumberDtoRequest {

    @JsonProperty("card_number")
    @NotBlank
    @Size(min = 16, max = 16, message = "Номер карты должен состоять из 16 цифр")
    @Schema(description = "Номер карты", example = "1234567812345678")
    private String number;
}
