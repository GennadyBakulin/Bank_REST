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

/**
 * Реализация сервиса для управления переводами между картами.
 * Предоставляет методы для выполнения переводов и получения истории переводов.
 */
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Выполняет перевод между двумя картами одного пользователя.
     * Проверяет принадлежность карт пользователю и достаточность средств на карте-отправителе.
     *
     * @param request объект TransferDtoRequest с данными для перевода
     * @return TransferDtoResponse с информацией о выполненном переводе
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException   если карты не принадлежат пользователю
     * @throws com.example.bankcards.exception.exceptions.ConflictRequestException  если попытка перевода на ту же карту
     */
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

    /**
     * Получает записи о всех переводах в системе с пагинацией.
     * Метод доступен только для администраторов.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество записей на странице
     * @return PageDtoResponse<TransferDtoResponse> страница с историей переводов
     */
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

    /**
     * Получает записи о всех переводах текущего пользователя с пагинацией.
     * Пользователь может видеть только свои собственные переводы.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество записей на странице
     * @return PageDtoResponse<TransferDtoResponse> страница с историей переводов пользователя
     */
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

    /**
     * Находит карту по её номеру в репозитории.
     *
     * @param cardNumber номер карты для поиска
     * @return сущность Card
     * @throws ResourceNotFoundException если карта с указанным номером не найдена
     */
    private Card findCardByNumber(String cardNumber) {
        return cardRepository.findByNumber(cardNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Card with number= " + cardNumber + " was not found"));
    }

    /**
     * Преобразует сущность Transfer в DTO объект TransferDtoResponse.
     *
     * @param transfer сущность перевода
     * @return TransferDtoResponse с данными о переводе
     */
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
