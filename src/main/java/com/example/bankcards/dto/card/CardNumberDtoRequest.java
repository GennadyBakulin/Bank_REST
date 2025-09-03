package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class CardNumberDtoRequest {

    @JsonProperty("card_number")
    @NotBlank
    @Length(min = 16, max = 16)
    private String number;
}
