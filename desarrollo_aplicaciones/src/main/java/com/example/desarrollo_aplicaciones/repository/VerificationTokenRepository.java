package com.example.desarrollo_aplicaciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByCode(String code);
    Optional<VerificationToken> findByUser(User user);

}