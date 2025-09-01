package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, String> {

    Optional<Card> findByNumber(String number);

    List<Card> findAllByUser_Email(String email);

    boolean existsByNumber(String cardNumber);
}
