package com.example.desarrollo_aplicaciones.entity;

import jakarta.persistence.*;
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
    private String estado;
}
