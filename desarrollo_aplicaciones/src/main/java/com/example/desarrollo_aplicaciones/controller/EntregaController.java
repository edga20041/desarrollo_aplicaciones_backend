package com.example.desarrollo_aplicaciones.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.desarrollo_aplicaciones.entity.Estado;
import com.example.desarrollo_aplicaciones.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.api.model.EntregaResponse;
import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.EntregaRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;

import com.example.desarrollo_aplicaciones.helpers.NombreEstado;

@RestController
@RequestMapping("/entregas")
@CrossOrigin(origins = "http://localhost:8000")
public class EntregaController {

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EstadoRepository estadoRepository;

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

    @GetMapping("/pendientes")
    public List<EntregaResponse> obtenerEntregasPendientes() {
        Estado estadoPendiente = estadoRepository.findByNombre(NombreEstado.Pendiente.toString());
        List<Entrega> entregasPendientes = entregaRepository.findByEstadoIdAndRepartidorIdNull(estadoPendiente.getId());
        return entregasPendientes.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
    }

    private EntregaResponse convertirAEntregaResponse(Entrega entrega) {
        EntregaResponse response = new EntregaResponse();
        response.setId(entrega.getId());
        response.setCliente(entrega.getCliente());
        response.setClienteDni(entrega.getClienteDni());
        response.setEstadoId(entrega.getEstadoId());
        response.setFechaCreacion(entrega.getFechaCreacion());
        response.setFechaAsignacion(entrega.getFechaAsignacion() != null ? entrega.getFechaAsignacion() : null);
        response.setRepartidorId(entrega.getRepartidorId());
        response.setFechaFinalizacion(
            entrega.getFechaFinalizacion() != null ? entrega.getFechaFinalizacion() : null
        );
        response.setRepartidorId(entrega.getRepartidorId());
        response.setRutaId(entrega.getRutaId());
        return response;
    }
}
