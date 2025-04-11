package com.example.desarrollo_aplicaciones.repository; // Ajusta el paquete

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.desarrollo_aplicaciones.entity.Entrega;

@Repository
public interface EntregaRepository extends JpaRepository<Entrega, Long> {
    List<Entrega> findByRepartidorId(Long repartidorId);
}