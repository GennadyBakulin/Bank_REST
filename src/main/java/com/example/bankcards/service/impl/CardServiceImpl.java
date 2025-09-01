package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.CardAlreadyExistsException;
import com.example.bankcards.exception.exceptions.CardNotFoundException;
import com.example.bankcards.exception.exceptions.CardNumberInvalidException;
import com.example.bankcards.exception.exceptions.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardUtils;
import com.example.bankcards.util.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    public CardDtoResponse create(CardDtoRequest request) {
        User user = findUserByEmail(request.getUserEmail());

        if (!CardUtils.validateCardNumber(request.getNumber())) {
            throw new CardNumberInvalidException("Invalid card number");
        }

        checkUniqueCardNumber(request.getNumber());

        Card card = new Card(
                request.getNumber(),
                user,
                UserUtils.getFullName(user),
                LocalDate.now().plusMonths(request.getCountMonth()),
                CardStatus.ACTIVE,
                request.getAmount()
        );

        Card saveCard = cardRepository.save(card);

        return CardDtoResponse.builder()
                .maskedCardNumber(CardUtils.getMaskedCardNumber(saveCard.getNumber()))
                .email(saveCard.getUser().getEmail())
                .fullNameUser(saveCard.getFullNameUser())
                .validityDate(saveCard.getExpirationDate())
                .status(saveCard.getStatus())
                .balance(saveCard.getBalance())
                .build();
    }

    @Override
    public void blockCard(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    public void activateCard(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    public void deleteCard(String cardNumber) {
        cardRepository.delete(findCardByNumber(cardNumber));
    }

    @Override
    public List<CardDtoResponse> getAllCards() {
        return cardRepository.findAll().stream().map(this::mapperToDto).toList();
    }

    @Override
    public List<CardDtoResponse> getAllCardsUser(String email) {
        return cardRepository.findAllByUser_Email(email).stream().map(this::mapperToDto).toList();
    }

    @Override
    public CardDtoResponse getCardByNumber(String cardNumber) {
        return mapperToDto(findCardByNumber(cardNumber));
    }

    @Override
    public void requestToBlockedCard(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        card.setRequestToBlocked(true);
        cardRepository.save(card);
    }

    private Card findCardByNumber(String cardNumber) {
        return cardRepository.findByNumber(cardNumber).orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    private void checkUniqueCardNumber(String cardNumber) {
        if (cardRepository.existsByNumber(cardNumber)) {
            throw new CardAlreadyExistsException("Card already exist");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email= %s not found".formatted(email)));
    }

    private CardDtoResponse mapperToDto(Card card) {
        return CardDtoResponse.builder()
                .maskedCardNumber(CardUtils.getMaskedCardNumber(card.getNumber()))
                .email(card.getUser().getEmail())
                .fullNameUser(card.getFullNameUser())
                .validityDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }
}
