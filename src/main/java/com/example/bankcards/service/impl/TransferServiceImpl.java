package com.example.bankcards.service.impl;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.transfer.Transfer;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.InvalidRequestException;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public TransferDtoResponse transferBetweenCardsOneUser(TransferDtoRequest request) {
        Card fromCard = findCardByNumber(request.getFromCardNumber());
        Card toCard = findCardByNumber(request.getToCardNumber());

        if (request.getFromCardNumber().equals(request.getToCardNumber())) {
            throw new InvalidRequestException("You can't make a transfer between the same card");
        }

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository
                .findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.equals(fromCard.getUser()) || !user.equals(toCard.getUser())) {
            throw new InvalidRequestException("One or both of the cards do not belong to the user");
        }

        if (fromCard.getStatus() != CardStatus.ACTIVE || toCard.getStatus() != CardStatus.ACTIVE) {
            throw new InvalidRequestException("The cards have no active status");
        }

        if (fromCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InvalidRequestException("There are not enough funds on the card from " +
                    "which the transfer is being made");
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

        return mapperToDto(saveTransfer);
    }

    @Override
    public PageDtoResponse<TransferDtoResponse> getAll(int pageNumber, int pageSize) {
        Page<Transfer> pageTransfers = transferRepository.findAll(PageRequest.of(pageNumber, pageSize));

        List<TransferDtoResponse> content = pageTransfers.getContent().stream()
                .map(this::mapperToDto)
                .toList();

        return new PageDtoResponse<>(
                content,
                pageTransfers.getTotalElements(),
                pageTransfers.getTotalPages(),
                pageTransfers.getNumber());
    }

    @Override
    public PageDtoResponse<TransferDtoResponse> getAllByUser(int pageNumber, int pageSize) {
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Transfer> pageTransfers = transferRepository
                .findAllByUser_Email(principal.getUsername(), PageRequest.of(pageNumber, pageSize));

        List<TransferDtoResponse> content = pageTransfers.getContent().stream()
                .map(this::mapperToDto)
                .toList();

        return new PageDtoResponse<>(
                content,
                pageTransfers.getTotalElements(),
                pageTransfers.getTotalPages(),
                pageTransfers.getNumber());
    }

    private Card findCardByNumber(String cardNumber) {
        return cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card with number= " + cardNumber + " was not found"));
    }

    private TransferDtoResponse mapperToDto(Transfer transfer) {

        return TransferDtoResponse.builder()
                .userEmail(transfer.getUser().getEmail())
                .fromCardNumber(transfer.getFromCardNumber())
                .toCardNumber(transfer.getToCardNumber())
                .amount(transfer.getAmount())
                .time(transfer.getTime())
                .build();
    }
}
