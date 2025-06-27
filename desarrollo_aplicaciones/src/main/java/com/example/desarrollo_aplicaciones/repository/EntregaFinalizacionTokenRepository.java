package com.example.desarrollo_aplicaciones.repository;

import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.EntregaFinalizacionToken;
import com.example.desarrollo_aplicaciones.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EntregaFinalizacionTokenRepository extends JpaRepository<EntregaFinalizacionToken, Long> {
    Optional<EntregaFinalizacionToken> findByEntrega(Entrega entrega);
    Optional<EntregaFinalizacionToken> findByToken(String token);
    Optional<EntregaFinalizacionToken> findByEntregaAndRepartidor(Entrega entrega, User repartidor);
} 