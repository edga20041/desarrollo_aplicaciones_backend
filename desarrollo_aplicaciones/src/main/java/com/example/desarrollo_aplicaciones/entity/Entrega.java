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

    @Column(name = "clienteDni", nullable = false)
    private Integer clienteDni;

    @Column(name = "estadoId", nullable = false)
    private Long estadoId;

    @Column(name = "repartidorId")
    private Long repartidorId;

    @Column(name = "rutaId", nullable = false)
    private Long rutaId;

    @Column(name = "producto", nullable = false)
    private String producto;

    @Column(name = "fechaCreacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fechaAsignacion")
    private LocalDateTime fechaAsignacion;

    @Column(name = "fechaFinalizacion")
    private LocalDateTime fechaFinalizacion;
}