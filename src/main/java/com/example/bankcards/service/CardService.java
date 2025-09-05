package com.example.bankcards.service;

import com.example.bankcards.dto.card.CardCreateDtoRequest;
import com.example.bankcards.dto.card.CardDtoResponse;
import com.example.bankcards.dto.card.TotalBalanceDtoResponse;
import com.example.bankcards.dto.page.PageDtoResponse;

/**
 * Сервис для управления банковскими картами.
 * Предоставляет методы для создания, блокировки, активации, удаления карт и получения информации о них.
 */
public interface CardService {

    /**
     * Создает новую банковскую карту.
     * Метод доступен только для администраторов.
     *
     * @param request объект CardCreateDtoRequest с данными для создания карты
     * @return CardDtoResponse с информацией о созданной карте
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если пользователь не найден
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException   если номер карты не валиден
     * @throws com.example.bankcards.exception.exceptions.ConflictRequestException  если карта с таким номером уже существует
     */
    CardDtoResponse create(CardCreateDtoRequest request);

    /**
     * Блокирует карту по её номеру.
     * Метод доступен только для администраторов.
     *
     * @param number номер карты для блокировки
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     */
    void blocked(String number);

    /**
     * Активирует карту по её номеру.
     * Метод доступен только для администраторов.
     *
     * @param number номер карты для активации
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.ConflictRequestException  если карта с истекшим сроком
     */
    void activation(String number);

    /**
     * Удаляет карту по её номеру.
     * Метод доступен только для администраторов.
     *
     * @param number номер карты для удаления
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     */
    void delete(String number);

    /**
     * Получает все карты в системе с пагинацией.
     * Метод доступен только для администраторов.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество карт на странице
     * @return PageDtoResponse<CardDtoResponse> страница со всеми картами системы
     */
    PageDtoResponse<CardDtoResponse> getAll(int pageNumber, int pageSize);

    /**
     * Получает все карты текущего пользователя с пагинацией.
     * Пользователь может видеть только свои собственные карты.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество карт на странице
     * @return PageDtoResponse<CardDtoResponse> страница с картами пользователя
     */
    PageDtoResponse<CardDtoResponse> getAllByUser(int pageNumber, int pageSize);

    /**
     * Получает информацию о карте по её номеру.
     * Пользователь может получить информацию только о своей карте.
     *
     * @param cardNumber номер карты
     * @return CardDtoResponse с информацией о карте
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException   если карта не принадлежит пользователю
     */
    CardDtoResponse getByNumber(String cardNumber);

    /**
     * Отправляет запрос на блокировку карты.
     * Пользователь может запросить блокировку только своих карт.
     *
     * @param cardNumber номер карты для блокировки
     * @throws com.example.bankcards.exception.exceptions.ResourceNotFoundException если карта не найдена
     * @throws com.example.bankcards.exception.exceptions.InvalidRequestException   если карта не принадлежит пользователю
     */
    void requestToBlocked(String cardNumber);

    /**
     * Получает общий баланс со всех карт текущего пользователя.
     * Суммирует балансы всех активных карт пользователя.
     *
     * @return TotalBalanceDtoResponse с общей суммой баланса всех активных карт
     */
    TotalBalanceDtoResponse getTotalBalanceUser();
}
