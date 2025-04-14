package com.example.desarrollo_aplicaciones.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "rutas_asignadas")
@Data
public class RutaAsignada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "repartidor_id", nullable = false)
    private Long repartidorId;

    @Column(nullable = false)
    private String estado; // pendiente, aceptada

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;
}