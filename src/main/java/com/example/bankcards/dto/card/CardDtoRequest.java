package com.example.bankcards.dto.card;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDtoRequest {

    @JsonProperty("card_number")
    @NotBlank
    @Length(min = 16, max = 16)
    private String number;

    @JsonProperty("user_email")
    @NotBlank
    private String userEmail;

    @JsonProperty("count_month")
    @NotBlank
    @Min(value = 1)
    private Integer countMonth;

    private BigDecimal amount;
}
