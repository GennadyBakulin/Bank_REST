package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardUtils;
import com.example.bankcards.util.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CardDtoResponse create(CardCreateDtoRequest request) {
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

        return mapperToDto(saveCard);
    }

    @Override
    public void blocked(String number) {
        Card card = findCardByNumber(number);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    public void activation(String number) {
        Card card = findCardByNumber(number);

        if (checkExpiredCard(card)) {
            throw new CardExpiredException("Card with number= " +
                    number + " has status expired and cannot be activated");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    public void delete(String number) {
        cardRepository.delete(findCardByNumber(number));
    }

    @Override
    @Transactional
    public PageDtoResponse<CardDtoResponse> getAll(int pageNumber, int pageSize) {
        Page<Card> pageCards = cardRepository.findAll(PageRequest.of(pageNumber, pageSize));

        List<CardDtoResponse> content = pageCards.getContent().stream()
                .map(this::mapperToDto)
                .toList();

        return new PageDtoResponse<>(
                content,
                pageCards.getTotalElements(),
                pageCards.getTotalPages(),
                pageCards.getNumber());
    }

    @Override
    @Transactional
    public PageDtoResponse<CardDtoResponse> getAllByUser(int pageNumber, int pageSize) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Card> pageCards = cardRepository
                .findAllByUser_Email(principal.getUsername(), PageRequest.of(pageNumber, pageSize));

        List<CardDtoResponse> content = pageCards.getContent().stream()
                .map(this::mapperToDto)
                .toList();

        return new PageDtoResponse<>(
                content,
                pageCards.getTotalElements(),
                pageCards.getTotalPages(),
                pageCards.getNumber());
    }

    @Override
    public CardDtoResponse getByNumber(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        checkBelongCardUser(card, principal);
        checkExpiredCard(card);

        return mapperToDto(card);
    }

    @Override
    public void requestToBlocked(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        checkBelongCardUser(card, principal);
        checkExpiredCard(card);

        card.setRequestToBlocked(true);
        cardRepository.save(card);
    }

    @Override
    public TotalBalanceDtoResponse getTotalBalanceUser() {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        BigDecimal totalBalance = cardRepository.findAllByUser_Email(principal.getUsername()).stream()
                .filter(card -> card.getStatus() == CardStatus.ACTIVE)
                .map(Card::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return TotalBalanceDtoResponse.builder()
                .email(principal.getUsername())
                .totalBalance(totalBalance)
                .build();
    }

    private void checkBelongCardUser(Card card, UserDetails principal) {
        if (!card.getUser().getEmail().equals(principal.getUsername())) {
            throw new CardNotFoundException("You has not card with number= " + card.getNumber());
        }
    }

    private boolean checkExpiredCard(Card card) {
        if (card.getExpirationDate().isBefore(LocalDate.now()) && card.getStatus() != CardStatus.EXPIRED) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
        return card.getStatus() == CardStatus.EXPIRED;
    }

    private Card findCardByNumber(String cardNumber) {
        return cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
    }

    private void checkUniqueCardNumber(String cardNumber) {
        if (cardRepository.existsByNumber(cardNumber)) {
            throw new CardAlreadyExistsException("Card already exist");
        }
    }

    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User with email= " + email + " not found"));
    }

    private CardDtoResponse mapperToDto(Card card) {
        return CardDtoResponse.builder()
                .maskedCardNumber(CardUtils.getMaskedCardNumber(card.getNumber()))
                .email(card.getUser().getEmail())
                .fullNameUser(card.getFullNameUser())
                .expirationDate(card.getExpirationDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }
}
