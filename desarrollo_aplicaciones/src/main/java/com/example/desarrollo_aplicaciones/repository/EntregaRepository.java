package com.example.desarrollo_aplicaciones.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.desarrollo_aplicaciones.entity.Entrega;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByRepartidorId(Long repartidorId);
    List<Entrega> findByRepartidorIdAndEstadoId(Long repartidorId, Long estadoId);
    List<Entrega> findByRepartidorIdAndEstadoIdIn(Long repartidorId, List<Long> estadoIds);
    List<Entrega> findByEstadoId(Long estadoId);
    List<Entrega> findByEstadoIdAndRepartidorIdNull(Long estadoId);
    int countByFechaCreacionAfter(LocalDateTime since);
}