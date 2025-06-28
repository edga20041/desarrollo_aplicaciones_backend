package com.example.desarrollo_aplicaciones.api.model;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EntregaResponse {
    private Long id;
    private String cliente;
    private Integer clienteDni;
    private Long estadoId;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAsignacion;
    private LocalDateTime fechaFinalizacion;
    private String producto;
    private Long repartidorId;
    private Long rutaId;
    private String area;
}