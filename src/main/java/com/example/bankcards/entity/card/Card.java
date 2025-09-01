package com.example.bankcards.entity.card;

import com.example.bankcards.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cards")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "cardNumber")
public class Card {

    @Id
    @Column(nullable = false)
    private String number;

    @ManyToOne
    @JoinColumn(name = "user_email", nullable = false)
    public User user;

    @Column(nullable = false)
    private String fullNameUser;

    @Column(nullable = false)
    private LocalDate expirationDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CardStatus status;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private Boolean requestToBlocked;

    public Card(
            String number,
            User user,
            String fullNameUser,
            LocalDate expirationDate,
            CardStatus status,
            BigDecimal balance) {
        this.number = number;
        this.user = user;
        this.fullNameUser = fullNameUser;
        this.expirationDate = expirationDate;
        this.status = status;
        this.balance = balance;
    }
}
