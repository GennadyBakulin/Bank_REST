package com.example.bankcards.service;

import com.example.bankcards.dto.page.PageDtoResponse;
import com.example.bankcards.dto.transfer.TransferDtoRequest;
import com.example.bankcards.dto.transfer.TransferDtoResponse;

/**
 * Сервис для управления переводами между картами.
 * Предоставляет методы для выполнения переводов и получения истории переводов.
 */
public interface TransferService {

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
    TransferDtoResponse transferBetweenCardsOneUser(TransferDtoRequest request);

    /**
     * Получает записи о всех переводах в системе с пагинацией.
     * Метод доступен только для администраторов.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество записей на странице
     * @return PageDtoResponse<TransferDtoResponse> страница с историей переводов
     */
    PageDtoResponse<TransferDtoResponse> getAll(int pageNumber, int pageSize);

    /**
     * Получает записи о всех переводах текущего пользователя с пагинацией.
     * Пользователь может видеть только свои собственные переводы.
     *
     * @param pageNumber номер страницы (начинается с 0)
     * @param pageSize   количество записей на странице
     * @return PageDtoResponse<TransferDtoResponse> страница с историей переводов пользователя
     */
    PageDtoResponse<TransferDtoResponse> getAllByUser(int pageNumber, int pageSize);
}
