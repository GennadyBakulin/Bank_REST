package com.example.bankcards.entity.user;

import com.example.bankcards.entity.card.Card;
import com.example.bankcards.entity.transfer.Transfer;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "email")
public class User {

    @Id
    @Column(name = "email", nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;


    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    public List<Card> cards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE)
    public List<Transfer> transfers;
}
