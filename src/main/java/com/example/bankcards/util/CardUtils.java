package com.example.bankcards.util;

public class CardUtils {

    public static boolean validateCardNumber(String cardNumber) {
        return cardNumber.matches("[0-9]{16}");
    }

    public static String getMaskedCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(12);
    }
}
