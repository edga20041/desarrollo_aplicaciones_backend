package com.example.desarrollo_aplicaciones.controller; // Ajusta el paquete

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.EntregaRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;

@RestController
@RequestMapping("/repartidores")
public class EntregaController {

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/historial")
    public List<Entrega> obtenerHistorialEntregas() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getRepartidorId() != null) {
            return entregaRepository.findByRepartidorId(user.getRepartidorId());
        } else {
            return List.of(); // O podrías devolver un error o una lista vacía según tu lógica
        }
    }
}