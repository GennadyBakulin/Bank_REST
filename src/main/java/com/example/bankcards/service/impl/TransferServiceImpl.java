package com.example.bankcards.service.impl;

import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.transfer.Transfer;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.exception.exceptions.InvalidTransactionRequest;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;

    @Override
    public TransferDtoResponse transferBetweenCardsOneUser(TransferDtoRequest request) {
        Card fromCard = cardRepository.findByNumber(request.getFromCardNumber())
                .orElseThrow(() -> new CardNotFoundException("Карта источник не найдена"));

        Card toCard = cardRepository.findByNumber(request.getToCardNumber())
                .orElseThrow(() -> new CardNotFoundException("Целевая карта не найдена"));

        if (request.getFromCardNumber().equals(request.getToCardNumber())) {
            throw new InvalidTransactionRequest("Нельзя сделать перевод между одной картой");
        }

//        if (!userEmail.equals(fromCard.getUserEmail()) || !userEmail.equals(toCard.getUserEmail())) {
//            throw new InvalidTransactionRequest("Карты не принадлежат пользователю!");
//        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidTransactionRequest("Карты не активны");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidTransactionRequest("На карте списания не достаточно средств!");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer transaction = new Transfer(
                principal,
                fromCard.getNumber(),
                toCard.getNumber(),
                request.getAmount(),
                LocalDateTime.now());

        Transfer saveTransaction = transferRepository.save(transaction);

        return TransferDtoResponse.builder()
                .fromCardNumber(saveTransaction.getFromCardNumber())
                .toCardNumber(saveTransaction.getToCardNumber())
                .amount(saveTransaction.getAmount())
                .time(saveTransaction.getTime())
                .build();
    }
}
