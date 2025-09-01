package com.example.bankcards.entity.card;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CardStatus {
    ACTIVE, BLOCKED, EXPIRED
}
