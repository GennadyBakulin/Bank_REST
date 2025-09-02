package com.example.bankcards.dto.card;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TotalBalanceDtoResponse {

    private String email;

    private BigDecimal totalBalance;
}
