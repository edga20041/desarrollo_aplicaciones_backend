package com.example.desarrollo_aplicaciones.api.model;

public class UserResponse {
    private Long id; // Cambiado a Long
    private String name;
    private String surname;
    private String email;
    private String phoneNumber;
    private Integer dni; // Se mantiene como Integer para coincidir con tu entidad User

    // Constructor vacío
    public UserResponse() {
    }

    // Constructor con todos los campos
    public UserResponse(Long id, String name, String surname, String email, String phoneNumber, Integer dni) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dni = dni;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Integer getDni() {
        return dni;
    }

    // Setters (opcionales)
    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setDni(Integer dni) {
        this.dni = dni;
    }
}