package com.example.bankcards.dto.transfer;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransferDtoResponse {

    private String userEmail;

    private String fromCardNumber;

    private String toCardNumber;

    private BigDecimal amount;

    private LocalDateTime time;
}
