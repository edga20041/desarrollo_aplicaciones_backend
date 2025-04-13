package com.example.desarrollo_aplicaciones.api.model;

import lombok.Data;

@Data
public class RutaResponse {
    private Long id;
    private String nombre;
    private String descripcion;
    private String origen;
    private String destino;
}
