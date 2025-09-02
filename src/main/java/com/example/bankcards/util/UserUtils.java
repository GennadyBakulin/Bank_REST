package com.example.bankcards.util;

import com.example.bankcards.entity.user.User;

public final class UserUtils {

    public static String getFullName(User user) {
        return user.getName() + " " + user.getLastName();
    }
}
