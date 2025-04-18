package com.example.desarrollo_aplicaciones.api.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RutaResponse {
    private Long id;

    private String nombre;

    private String descripcion;

    private String origen;

    private String destino;

    private Double latitudOrigen;

    private Double longitudOrigen;

    private Double latitudDestino;

    private Double longitudDestino;

    private LocalDateTime fechaCreacion;
}
