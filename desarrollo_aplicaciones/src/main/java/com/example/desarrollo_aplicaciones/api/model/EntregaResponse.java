package com.example.desarrollo_aplicaciones.api.model;

import lombok.Data;

@Data
public class EntregaResponse {

    private Long id;
    private String tiempoEntrega;
    private String cliente;
    private String estadoFinal;
    private Boolean aceptada;
    private String tiempoDecision;
}
