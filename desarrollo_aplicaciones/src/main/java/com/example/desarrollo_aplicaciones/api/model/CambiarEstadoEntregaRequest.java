package com.example.desarrollo_aplicaciones.api.model;

import lombok.Data;

@Data
public class CambiarEstadoEntregaRequest {
    private Long entregaId;
    private Long estadoId;
    private Long repartidorId;
}
