package com.example.bankcards.repository;

import com.example.bankcards.entity.card.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, String> {

    Optional<Card> findByNumber(String number);

    Page<Card> findAllByUser_Email(String email, Pageable pageable);

    List<Card> findAllByUser_Email(String email);

    boolean existsByNumber(String cardNumber);
}
