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

    @Override
    public boolean isValid(String token, UserDetails user) {

        String username = extractUsername(token);

        boolean isValidToken = tokenRepository.findByAccessToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidToken;
    }

    @Override
    public boolean isValidRefresh(String token, User user) {

        String username = extractUsername(token);

        boolean isValidRefreshToken = tokenRepository.findByRefreshToken(token)
                .map(t -> !t.isLoggedOut()).orElse(false);

        return username.equals(user.getUsername())
                && isAccessTokenExpired(token)
                && isValidRefreshToken;
    }

    @Override
    public void revokeAllToken(User user) {

        List<Token> validTokens = tokenRepository.findAllAccessTokenByUser_Email(user.getEmail());

        if (!validTokens.isEmpty()) {
            validTokens.forEach(t -> t.setLoggedOut(true)
            );
        }

        tokenRepository.saveAll(validTokens);
    }

    @Override
    public void saveUserToken(String accessToken, String refreshToken, User user) {

        Token token = new Token();

        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setLoggedOut(false);
        token.setUser(user);

        tokenRepository.save(token);
    }

    private boolean isAccessTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {

        JwtParserBuilder parser = Jwts.parser();

        parser.verifyWith(getSgningKey());

        return parser.build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String generateAccessToken(User user) {

        return generateToken(user, accessTokenExpiration);
    }

    @Override
    public String generateRefreshToken(User user) {

        return generateToken(user, refreshTokenExpiration);
    }

    private String generateToken(User user, long expiryTime) {
        JwtBuilder builder = Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiryTime))
                .signWith(getSgningKey());

        return builder.compact();
    }

    private SecretKey getSgningKey() {

        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }
}
