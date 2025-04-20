package com.example.desarrollo_aplicaciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.desarrollo_aplicaciones.entity.PasswordResetToken;
import com.example.desarrollo_aplicaciones.entity.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
    Optional<PasswordResetToken> findByUserAndToken(User user, String token); 

}
