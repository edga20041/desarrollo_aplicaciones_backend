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
@Table(name = "entregas")
@Data
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente", nullable = false)
    private String cliente;

    @Column(name = "cliente_dni", nullable = false)
    private Integer clienteDni;

    @Column(name = "estado_id", nullable = false)
    private Long estadoId;

    @Column(name = "repartidor_id")
    private Long repartidorId;

    @Column(name = "ruta_id", nullable = false)
    private Long rutaId;

    @Column(name = "producto", nullable = false)
    private String producto;

    @Column(name = "area", nullable = false)
    private String area;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_asignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;
}