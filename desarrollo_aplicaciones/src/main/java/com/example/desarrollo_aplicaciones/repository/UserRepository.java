package com.example.desarrollo_aplicaciones.repository; // Asegúrate de que este paquete coincida con la ubicación de tu interfaz

import java.util.Optional; // Importa tu clase User

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.desarrollo_aplicaciones.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

}