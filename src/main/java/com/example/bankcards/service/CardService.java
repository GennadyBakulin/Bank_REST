package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CardService {

    /**
     * Администратор создает новую банковскую карту
     */
    CardDtoResponse create(CardDtoRequest request);

    /**
     * Блокирует карту
     */
    void blockCard(String cardNumber);

    /**
     * Администратор активирует карту
     */
    void activateCard(String cardNumber);

    /**
     * Администратор удаляет карту по номеру
     */
    void deleteCard(String cardNumber);

    /**
     * Администратор находит все карты без пагинациии!!!
     */
    List<CardDtoResponse> getAllCards();

    /**
     * Находит все карты пользователя без пагинациии!!!
     */
    List<CardDtoResponse> getAllCardsUser();

    /**
     * Пользователь просматривает свои карты без пагинациии!!!
     */
    CardDtoResponse getCardByNumber(String cardNumber);

    /**
     * Пользователь отправляет запрос на блокировку карты
     */
    void requestToBlockedCard(String cardNumber);

    /**
     * Пользователь получает общий баланс со всех своих карт
     */
    TotalBalanceDtoResponse getTotalBalanceUser();
}
