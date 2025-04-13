package com.example.desarrollo_aplicaciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.desarrollo_aplicaciones.entity.RutaAsignada;

public interface RutaAsignadaRepository extends JpaRepository<RutaAsignada, Long> {
    List<RutaAsignada> findByRepartidorIdAndEstado(Long repartidorId, String estado);
    Optional<RutaAsignada> findByRutaIdAndRepartidorId(Long rutaId, Long repartidorId);
}
