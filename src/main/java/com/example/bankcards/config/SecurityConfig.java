package com.example.bankcards.config;

import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.security.handler.CustomAccessDeniedHandler;
import com.example.bankcards.security.handler.CustomLogoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Список публичных endpoints для Swagger и API документации
    private static final String[] SWAGGER_WHITELIST = {
            "/swagger-ui/**",
            "/api-docs/**"
    };

    // Список публичных endpoints для аутентификации
    private static final String[] AUTH_WHITELIST = {
            "/error",
            "/login",
            "/registration",
            "/refresh_token/**"
    };

    // Список endpoints для роли ADMIN
    private static final String[] ADMIN_LIST = {
            "/admin/**"
    };

    // Список endpoints для роли USER
    private static final String[] USER_LIST = {
            "/user/**"
    };

    private final JwtFilter jwtFIlter;

    private final UserDetailsServiceImpl userDetailsService;

    private final CustomAccessDeniedHandler accessDeniedHandler;

    private final CustomLogoutHandler customLogoutHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers(SWAGGER_WHITELIST).permitAll();
                    auth.requestMatchers(AUTH_WHITELIST).permitAll();
                    auth.requestMatchers(ADMIN_LIST).hasAuthority("ADMIN");
                    auth.requestMatchers(USER_LIST).hasAuthority("USER");
                    auth.anyRequest().authenticated();
                })
                .userDetailsService(userDetailsService)
                .exceptionHandling(e -> {
                    e.accessDeniedHandler(accessDeniedHandler);
                    e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
                })
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterBefore(jwtFIlter, UsernamePasswordAuthenticationFilter.class)
                .logout(log -> {
                    log.logoutUrl("/logout");
                    log.addLogoutHandler(customLogoutHandler);
                    log.logoutSuccessHandler((request, response, authentication) ->
                            SecurityContextHolder.clearContext());
                });

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
