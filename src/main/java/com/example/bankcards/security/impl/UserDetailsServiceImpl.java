package com.example.bankcards.security.impl;

import com.example.bankcards.entity.user.User;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Сервис для загрузки данных пользователя в контексте Spring Security.
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по email для аутентификации и авторизации.
     * Преобразует сущность User в UserDetails, необходимый для Spring Security.
     *
     * @param email email пользователя, используемый в качестве имени пользователя
     * @return UserDetails объект, содержащий информацию о пользователе для Spring Security
     * @throws UsernameNotFoundException если пользователь с указанным email не найден в базе данных
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString())));
    }
}
