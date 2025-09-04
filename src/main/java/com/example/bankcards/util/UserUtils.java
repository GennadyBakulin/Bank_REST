package com.example.bankcards.util;

import com.example.bankcards.entity.user.User;

public final class UserUtils {

    public static String getFullName(User user) {
        return user.getName() + " " + user.getLastName();
    }

    public static boolean isValidPassword(String password) {
        return password.matches("[0-9a-zA-Z]{5,16}");
    }
}
