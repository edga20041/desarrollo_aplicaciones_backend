package com.example.desarrollo_aplicaciones.api.model;

import lombok.Data;

@Data
public class EntregaResponse {
    private Long id;
    private String tiempoEntrega;
    private String cliente;
    private Long estadoId;
    private String fechaFinalizacion; 

}