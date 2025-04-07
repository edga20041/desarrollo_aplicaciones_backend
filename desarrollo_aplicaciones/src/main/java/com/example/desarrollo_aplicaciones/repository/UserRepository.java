package com.example.desarrollo_aplicaciones.repository; // Asegúrate de que este paquete coincida con la ubicación de tu interfaz

import java.util.Optional; // Importa tu clase User

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.desarrollo_aplicaciones.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    // Spring Data JPA generará automáticamente la implementación de este método
    // para buscar un usuario por su dirección de correo electrónico.

    // Puedes agregar más métodos personalizados aquí si los necesitas,
    // por ejemplo, para buscar usuarios por nombre, etc.
}