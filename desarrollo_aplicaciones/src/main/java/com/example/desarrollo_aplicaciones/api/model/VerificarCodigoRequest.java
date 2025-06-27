package com.example.desarrollo_aplicaciones.api.model;

public class VerificarCodigoRequest {
    private Long entregaId;
    private Long repartidorId;
    private String codigo;

    public Long getEntregaId() { return entregaId; }
    public void setEntregaId(Long entregaId) { this.entregaId = entregaId; }

    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
} 