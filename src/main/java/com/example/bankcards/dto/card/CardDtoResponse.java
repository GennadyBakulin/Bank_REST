package com.example.bankcards.dto.card;

import com.example.bankcards.entity.card.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class CardDtoResponse {

    private String maskedCardNumber;

    private String email;

    private String fullNameUser;

    private LocalDate expirationDate;

    private CardStatus status;

    private BigDecimal balance;
}
