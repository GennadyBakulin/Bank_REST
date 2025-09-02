package com.example.bankcards.service;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;

public interface TransferService {

    /**
     * Пользователь осуществляет перевод средств между двумя картами своими картами
     */
    TransferDtoResponse transferBetweenCardsOneUser(TransferDtoRequest request);

    /**
     * Администратор получает записи о всех переводах с пагинацией
     */
    PageDtoResponse<TransferDtoResponse> getAll(int pageNumber, int pageSize);

    /**
     * Пользователь получает записи о всех своих переводах с пагинацией
     */
    PageDtoResponse<TransferDtoResponse> getAllByUser(int pageNumber, int pageSize);
}
