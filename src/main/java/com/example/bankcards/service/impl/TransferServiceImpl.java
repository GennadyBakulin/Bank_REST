package com.example.bankcards.service.impl;

import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.transfer.Transfer;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.exception.exceptions.InvalidTransactionRequest;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    public TransferDtoResponse transferBetweenCardsOneUser(TransferDtoRequest request) {
        Card fromCard = findCardByNumber(request.getFromCardNumber());
        Card toCard = findCardByNumber(request.getToCardNumber());

        if (request.getFromCardNumber().equals(request.getToCardNumber())) {
            throw new InvalidTransactionRequest("You can't make a transfer between the same card");
        }

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.equals(fromCard.getUser()) || !user.equals(toCard.getUser())) {
            throw new InvalidTransactionRequest("One or both of the cards do not belong to the user");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidTransactionRequest("The cards have no active status");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidTransactionRequest("There are not enough funds on the card from which the transfer is being made");
        }

        fromCard.setBalance(fromCard.getBalance().subtract(request.getAmount()));
        toCard.setBalance(toCard.getBalance().add(request.getAmount()));

        Transfer transfer = new Transfer(
                user,
                fromCard.getNumber(),
                toCard.getNumber(),
                request.getAmount(),
                LocalDateTime.now());

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
        Transfer saveTransfer = transferRepository.save(transfer);

        return TransferDtoResponse.builder()
                .fromCardNumber(saveTransfer.getFromCardNumber())
                .toCardNumber(saveTransfer.getToCardNumber())
                .amount(saveTransfer.getAmount())
                .time(saveTransfer.getTime())
                .build();
    }

    private Card findCardByNumber(String cardNumber) {
        return cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card with number= " + cardNumber + " was not found"));
    }
}
