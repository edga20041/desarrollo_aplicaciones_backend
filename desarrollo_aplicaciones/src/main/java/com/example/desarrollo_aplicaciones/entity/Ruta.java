package com.example.desarrollo_aplicaciones.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "rutas")
@Data
public class Ruta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column
    private String descripcion;

    @Column(nullable = false)
    private String origen;

    @Column(nullable = false)
    private String destino;

    @Enumerated(EnumType.STRING) // Almacena el nombre del enum como texto en la base de datos
    @Column(nullable = false)
    private EstadoRuta estado;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private java.time.LocalDateTime fechaCreacion;
}