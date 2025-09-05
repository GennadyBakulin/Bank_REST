package com.example.bankcards.util;

/**
 * Утилитарный класс для работы с банковскими картами.
 * Содержит методы для валидации и маскировки номеров карт.
 */
public final class CardUtils {

    /**
     * Проверяет валидность номера банковской карты.
     * Номер карты должен состоять ровно из 16 цифр.
     *
     * @param cardNumber номер карты для проверки
     * @return true если номер карты валиден, иначе false
     */
    public static boolean validateCardNumber(String cardNumber) {
        return cardNumber.matches("[0-9]{16}");
    }

    /**
     * Маскирует номер банковской карты для безопасного отображения.
     * Заменяет первые 12 цифр на звездочки, оставляя видимыми последние 4 цифры.
     * Формат маскировки: "**** **** **** XXXX", где XXXX - последние 4 цифры номера карты.
     *
     * @param cardNumber номер карты для маскировки
     * @return маскированный номер карты в формате "**** **** **** XXXX"
     */
    public static String getMaskedCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(12);
    }
}
