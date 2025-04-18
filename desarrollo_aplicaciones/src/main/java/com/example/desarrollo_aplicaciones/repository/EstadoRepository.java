package com.example.desarrollo_aplicaciones.repository;

import com.example.desarrollo_aplicaciones.entity.Estado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Long> {
    Estado findByNombre(String nombre);
}
