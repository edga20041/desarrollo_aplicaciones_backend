package com.example.desarrollo_aplicaciones.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class EntregaFinalizacionToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    @ManyToOne
    @JoinColumn(name = "entrega_id", referencedColumnName = "id")
    private Entrega entrega;

    @ManyToOne
    @JoinColumn(name = "repartidor_id", referencedColumnName = "id")
    private User repartidor;

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDateTime expirationDate) { this.expirationDate = expirationDate; }

    public Entrega getEntrega() { return entrega; }
    public void setEntrega(Entrega entrega) { this.entrega = entrega; }

    public User getRepartidor() { return repartidor; }
    public void setRepartidor(User repartidor) { this.repartidor = repartidor; }
} 