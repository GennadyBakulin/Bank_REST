package com.example.bankcards.util;

import com.example.bankcards.entity.user.User;

/**
 * Утилитарный класс для работы с пользователями.
 * Содержит методы для создания полного имени пользователей и валидации паролей.
 */
public final class UserUtils {

    /**
     * Формирует полное имя пользователя из имени и фамилии.
     *
     * @param user объект пользователя
     * @return строка с полным именем в формате "Имя Фамилия"
     */
    public static String getFullName(User user) {
        return user.getName() + " " + user.getLastName();
    }

    /**
     * Проверяет валидность пароля пользователя.
     * Пароль должен содержать от 5 до 16 символов, включая цифры и буквы (латинские) в верхнем и нижнем регистре.
     *
     * @param password пароль для проверки
     * @return true если пароль соответствует требованиям, иначе false
     */
    public static boolean isValidPassword(String password) {
        return password.matches("[0-9a-zA-Z]{5,16}");
    }
}
