package com.example.desarrollo_aplicaciones.repository;



import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.desarrollo_aplicaciones.entity.RutaRechazada;


public interface RutaRechazadaRepository extends JpaRepository<RutaRechazada, Long> {
    List<RutaRechazada> findByRepartidorId(Long repartidorId);
    Optional<RutaRechazada> findByRutaIdAndRepartidorId(Long rutaId, Long repartidorId);
}