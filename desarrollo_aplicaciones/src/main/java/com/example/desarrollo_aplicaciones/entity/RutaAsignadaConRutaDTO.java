package com.example.desarrollo_aplicaciones.entity;

public class RutaAsignadaConRutaDTO {
    private Long id;
    private Ruta ruta;
    private Long repartidorId;
    private String estado;

    public RutaAsignadaConRutaDTO() {
    }

    public RutaAsignadaConRutaDTO(Long id, Ruta ruta, Long repartidorId, String estado) {
        this.id = id;
        this.ruta = ruta;
        this.repartidorId = repartidorId;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}