package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.desarrollo_aplicaciones.api.model.EntregaRequest;
import com.example.desarrollo_aplicaciones.api.model.EntregaResponse;
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
    public List<EntregaResponse> obtenerHistorialEntregas() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null && user.getRepartidorId() != null) {
            List<Entrega> entregas = entregaRepository.findByRepartidorId(user.getRepartidorId());
            return entregas.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @PostMapping("/aceptar")
    public EntregaResponse aceptarEntrega(@RequestBody EntregaRequest entregaRequest) {
        Entrega entrega = entregaRepository.findById(entregaRequest.getId()).orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        entrega.setAceptada(true);
        entrega.setTiempoDecision(LocalDateTime.now().toString());
        entrega.setEstadoFinal("Aceptada");
        entregaRepository.save(entrega);
        return convertirAEntregaResponse(entrega);
    }

    @PostMapping("/rechazar")
    public EntregaResponse rechazarEntrega(@RequestBody EntregaRequest entregaRequest) {
        Entrega entrega = entregaRepository.findById(entregaRequest.getId()).orElseThrow(() -> new RuntimeException("Entrega no encontrada"));
        entrega.setAceptada(false);
        entrega.setTiempoDecision(LocalDateTime.now().toString());
        entrega.setEstadoFinal("Rechazada");
        entregaRepository.save(entrega);
        return convertirAEntregaResponse(entrega);
    }

    private EntregaResponse convertirAEntregaResponse(Entrega entrega) {
        EntregaResponse response = new EntregaResponse();
        response.setId(entrega.getId());
        response.setTiempoEntrega(entrega.getTiempoEntrega());
        response.setCliente(entrega.getCliente());
        response.setEstadoFinal(entrega.getEstadoFinal());
        response.setAceptada(entrega.getAceptada());
        response.setTiempoDecision(entrega.getTiempoDecision());
        return response;
    }
}
