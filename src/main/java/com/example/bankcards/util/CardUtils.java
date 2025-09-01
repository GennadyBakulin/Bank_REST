package com.example.bankcards.util;

import com.example.bankcards.entity.user.User;

public class CardUtils {

    public static boolean validateCardNumber(String cardNumber) {
        return cardNumber.matches("[0-9]{16}");
    }

    public static String getMaskedCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(12);
    }

    public static String getFullName(User user) {
        return user.getName() + " " + user.getLastName();
    }
}
