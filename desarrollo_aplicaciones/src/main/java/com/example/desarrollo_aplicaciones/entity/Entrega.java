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

    @Column(name = "tiempo_entrega")
    private String tiempoEntrega;

    @Column(name = "cliente")
    private String cliente;

    @Column(name = "estado_final")
    private String estadoFinal;

    @Column(name = "repartidor_id", nullable = false)
    private Long repartidorId;
    
    @Column(name = "fecha_finalizacion")
    private LocalDateTime fechaFinalizacion;
}