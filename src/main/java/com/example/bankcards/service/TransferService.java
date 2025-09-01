package com.example.bankcards.service;

import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;

public interface TransferService {

    /**
     * Перевод денежных средств между двумя картами одного пользователя
     */
    TransferDtoResponse transferBetweenCardsOneUser(TransferDtoRequest request);
}
