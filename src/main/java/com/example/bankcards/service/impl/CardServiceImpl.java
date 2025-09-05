package com.example.bankcards.service.impl;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.card.CardStatus;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.exceptions.ConflictRequestException;
import com.example.bankcards.exception.exceptions.InvalidRequestException;
import com.example.bankcards.exception.exceptions.ResourceNotFoundException;
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

/**
 * Реализация сервиса для управления банковскими картами.
 * Предоставляет методы для создания, блокировки, активации, удаления карт и получения информации о них.
 */
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    /**
     * Создает новую банковскую карту.
     * Метод доступен только для администраторов.
     *
     * @param request объект CardCreateDtoRequest с данными для создания карты
     * @return CardDtoResponse с информацией о созданной карте
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если пользователь не найден
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException если номер карты не валиден
     * @throws com.example.bankcards.exception.exceptions.ConflictRequestException  если карта с таким номером уже существует
     */
    @Override
    @Transactional
    public CardDtoResponse create(CardCreateDtoRequest request) {
        User user = findUserByEmail(request.getUserEmail());

        if (!CardUtils.validateCardNumber(request.getNumber())) {
            throw new InvalidRequestException("Invalid card number");
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

    /**
     * Блокирует карту по её номеру.
     * Метод доступен только для администраторов.
     *
     * @param number номер карты для блокировки
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     */
    @Override
    public void blocked(String number) {
        Card card = findCardByNumber(number);
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    /**
     * Активирует карту по её номеру.
     * Метод доступен только для администраторов.
     *
     * @param number номер карты для активации
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.ConflictRequestException если карта с истекшим сроком
     */
    @Override
    public void activation(String number) {
        Card card = findCardByNumber(number);

        if (checkExpiredCard(card)) {
            throw new ConflictRequestException("Card with number= " +
                    number + " has status expired and cannot be activated");
        }

        card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    /**
     * Удаляет карту по её номеру.
     * Метод доступен только для администраторов.
     *
     * @param number номер карты для удаления
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     */
    @Override
    public void delete(String number) {
        cardRepository.delete(findCardByNumber(number));
    }

    /**
     * Получает все карты в системе с пагинацией.
     * Метод доступен только для администраторов.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество карт на странице
     * @return PageDtoResponse<CardDtoResponse> страница со всеми картами системы
     */
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

    /**
     * Получает все карты текущего пользователя с пагинацией.
     * Пользователь может видеть только свои собственные карты.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество карт на странице
     * @return PageDtoResponse<CardDtoResponse> страница с картами пользователя
     */
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

    /**
     * Получает информацию о карте по её номеру.
     * Пользователь может получить информацию только о своей карте.
     *
     * @param cardNumber номер карты
     * @return CardDtoResponse с информацией о карте
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException   если карта не принадлежит пользователю
     */
    @Override
    public CardDtoResponse getByNumber(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        checkBelongCardUser(card, principal);
        checkExpiredCard(card);

        return mapperToDto(card);
    }

    /**
     * Отправляет запрос на блокировку карты.
     * Пользователь может запросить блокировку только своих карт.
     *
     * @param cardNumber номер карты для блокировки
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException   если карта не принадлежит пользователю
     */
    @Override
    public void requestToBlocked(String cardNumber) {
        Card card = findCardByNumber(cardNumber);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        checkBelongCardUser(card, principal);
        checkExpiredCard(card);

        card.setRequestToBlocked(true);
        cardRepository.save(card);
    }

    /**
     * Получает общий баланс со всех карт текущего пользователя.
     * Суммирует балансы всех активных карт пользователя.
     *
     * @return TotalBalanceDtoResponse с общей суммой баланса всех активных карт
     */
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

    /**
     * Проверяет принадлежность карты пользователю.
     *
     * @param card карта для проверки
     * @param principal данные аутентифицированного пользователя
     * @throws ResourceNotFoundException если карта не принадлежит пользователю
     */
    private void checkBelongCardUser(Card card, UserDetails principal) {
        if (!card.getUser().getEmail().equals(principal.getUsername())) {
            throw new ResourceNotFoundException("You has not card with number= " + card.getNumber());
        }
    }

    /**
     * Проверяет истечение срока действия карты.
     * Автоматически обновляет статус карты на EXPIRED при истечении срока.
     *
     * @param card карта для проверки
     * @return true если карта просрочена, иначе false
     */
    private boolean checkExpiredCard(Card card) {
        if (card.getExpirationDate().isBefore(LocalDate.now()) && card.getStatus() != CardStatus.EXPIRED) {
            card.setStatus(CardStatus.EXPIRED);
            cardRepository.save(card);
        }
        return card.getStatus() == CardStatus.EXPIRED;
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
                .orElseThrow(() -> new ResourceNotFoundException("Card not found"));
    }

    /**
     * Проверяет уникальность номера карты в системе.
     *
     * @param cardNumber номер карты для проверки
     * @throws ConflictRequestException если карта с таким номером уже существует
     */
    private void checkUniqueCardNumber(String cardNumber) {
        if (cardRepository.existsByNumber(cardNumber)) {
            throw new ConflictRequestException("Card already exist");
        }
    }

    /**
     * Находит пользователя по email в репозитории.
     *
     * @param email email пользователя для поиска
     * @return сущность User
     * @throws ResourceNotFoundException если пользователь с указанным email не найден
     */
    private User findUserByEmail(String email) {
        return userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email= " + email + " not found"));
    }

    /**
     * Преобразует сущность Card в DTO объект CardDtoResponse.
     * Маскирует номер карты для безопасности.
     *
     * @param card сущность карты
     * @return CardDtoResponse с данными карты
     */
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
