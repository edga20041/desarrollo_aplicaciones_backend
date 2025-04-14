package com.example.desarrollo_aplicaciones.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "rutas_rechazadas")
@Data
public class RutaRechazada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "repartidor_id", nullable = false)
    private Long repartidorId;

    @Column(name = "fecha_rechazo", nullable = false, updatable = false)
    private LocalDateTime fechaRechazo;
}