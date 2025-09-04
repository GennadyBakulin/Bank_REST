package com.example.bankcards.security.impl;

import com.example.bankcards.entity.token.Token;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.repository.TokenRepository;
import com.example.bankcards.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Реализация сервиса для работы с JWT (JSON Web Token) токенами.
 * Обеспечивает генерацию, валидацию, извлечение данных из токенов и управление токенами.
 */
@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${security.jwt.secret_key}")
    private String secretKey;

    @Value("${security.jwt.access_token_expiration}")
    private long accessTokenExpiration;

    @Value("${security.jwt.refresh_token_expiration}")
    private long refreshTokenExpiration;

    private final TokenRepository tokenRepository;

    /**
     * Проверяет валидность access токена для указанного пользователя.
     *
     * @param token JWT токен для проверки
     * @param user  данные пользователя для верификации
     * @return true если токен валиден и не отозван, иначе false
     */
    @Override
    public boolean isValid(String token, UserDetails user) {

        String username = extractUsername(token);

        boolean isValidToken = tokenRepository.findByAccessToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidToken;
    }

    /**
     * Проверяет валидность refresh токена для указанного пользователя.
     *
     * @param token refresh токен для проверки
     * @param user  пользователь для верификации
     * @return true если refresh токен валиден и не отозван, иначе false
     */
    @Override
    public boolean isValidRefresh(String token, User user) {

        String username = extractUsername(token);

        boolean isValidRefreshToken = tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidRefreshToken;
    }

    /**
     * Отзывает все токены пользователя.
     * Помечает все активные токены пользователя как отозванные (loggedOut = true).
     *
     * @param user пользователь, токены которого нужно отозвать
     */
    @Override
    public void revokeAllToken(User user) {

        List<Token> validTokens = tokenRepository.findAllAccessTokenByUser_Email(user.getEmail());

        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setLoggedOut(true)
            );
        }

        tokenRepository.saveAll(validTokens);
    }

    /**
     * Сохраняет сгенерированные токены для пользователя в базе данных.
     *
     * @param accessToken  access токен
     * @param refreshToken refresh токен
     * @param user         пользователь, для которого сохраняются токены
     */
    @Override
    public void saveUserToken(String accessToken, String refreshToken, User user) {

        Token token = new Token();

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);

        tokenRepository.save(token);
    }

    /**
     * Проверяет, не истек ли срок действия access токена.
     *
     * @param token JWT токен для проверки
     * @return true если токен не истек, иначе false
     */
    private boolean isAccessTokenExpired(String token) {

        return !extractExpiration(token).before(new Date());
    }

    /**
     * Извлекает дату истечения срока действия токена.
     *
     * @param token JWT токен
     * @return дата истечения срока действия токена
     */
    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Извлекает имя пользователя (email) из JWT токена.
     *
     * @param token JWT токен
     * @return имя пользователя (email), содержащееся в токене
     */
    @Override
    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Извлекает конкретное утверждение из JWT токена.
     *
     * @param <T>      тип возвращаемого значения
     * @param token    JWT токен
     * @param resolver функция для извлечения и преобразования утверждения
     * @return значение утверждения указанного типа
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    /**
     * Извлекает все утверждения из JWT токена.
     *
     * @param token JWT токен
     * @return объект Claims со всеми утверждениями токена
     */
    private Claims extractAllClaims(String token) {
        JwtParserBuilder parser = Jwts.parser();
        parser.verifyWith(getSgningKey());

        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Генерирует access токен для указанного пользователя.
     *
     * @param user пользователь, для которого генерируется токен
     * @return сгенерированный JWT access токен
     */
    @Override
    public String generateAccessToken(User user) {

        return generateToken(user, accessTokenExpiration);
    }

    /**
     * Генерирует refresh токен для указанного пользователя.
     *
     * @param user пользователь, для которого генерируется токен
     * @return сгенерированный JWT refresh токен
     */
    @Override
    public String generateRefreshToken(User user) {

        return generateToken(user, refreshTokenExpiration);
    }

    /**
     * Генерирует JWT токен с указанными параметрами.
     *
     * @param user       пользователь, для которого генерируется токен
     * @param expiryTime время жизни токена в миллисекундах
     * @return сгенерированный JWT токен
     */
    private String generateToken(User user, long expiryTime) {
        JwtBuilder builder = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(getSgningKey());

        return builder.compact();
    }

    /**
     * Создает секретный ключ для подписи и верификации JWT токенов.
     * Декодирует base64url-encoded секретный ключ из конфигурации.
     *
     * @return SecretKey для работы с JWT
     */
    private SecretKey getSgningKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
