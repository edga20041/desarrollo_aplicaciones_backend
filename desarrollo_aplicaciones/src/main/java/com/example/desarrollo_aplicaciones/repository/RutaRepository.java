package com.example.desarrollo_aplicaciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.desarrollo_aplicaciones.entity.Ruta;

public interface RutaRepository extends JpaRepository<Ruta, Long> {
}