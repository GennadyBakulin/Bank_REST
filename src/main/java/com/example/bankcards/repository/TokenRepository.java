package com.example.bankcards.repository;

import com.example.bankcards.entity.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            SELECT t FROM Token t inner join User u
            on t.user.email = u.email
            where t.user.email = :email and t.loggedOut = false
            """)
    List<Token> findAllAccessTokenByUser_Email(String email);

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByRefreshToken(String refreshToken);
}
