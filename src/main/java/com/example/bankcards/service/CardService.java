package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;

public interface CardService {

    /**
     * Администратор создает новую банковскую карту
     */
    CardDtoResponse create(CardCreateDtoRequest request);

    /**
     * Администратор блокирует карту по номеру
     */
    void blocked(String number);

    /**
     * Администратор активирует карту по номеру
     */
    void activation(String number);

    /**
     * Администратор удаляет карту по номеру
     */
    void delete(String number);

    /**
     * Администратор находит все карты с пагинацией
     */
    PageDtoResponse<CardDtoResponse> getAll(int pageNumber, int pageSize);

    /**
     * Пользователь получает все свои карты с пагинацией
     */
    PageDtoResponse<CardDtoResponse> getAllByUser(int pageNumber, int pageSize);

    /**
     * Пользователь получает карту по её номеру
     */
    CardDtoResponse getByNumber(String cardNumber);

    /**
     * Пользователь отправляет запрос на блокировку карты
     */
    void requestToBlocked(String cardNumber);

    /**
     * Пользователь получает общий баланс со всех своих карт
     */
    TotalBalanceDtoResponse getTotalBalanceUser();
}
