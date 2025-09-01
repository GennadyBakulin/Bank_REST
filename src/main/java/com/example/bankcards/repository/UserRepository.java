package com.example.bankcards.repository;

import com.example.bankcards.entity.user.User;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    void deleteByEmail(String email);

    boolean existsByEmail(String email);

    void delete(@NonNull User user);
}
