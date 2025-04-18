package com.example.desarrollo_aplicaciones.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.desarrollo_aplicaciones.entity.Estado;
import com.example.desarrollo_aplicaciones.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.desarrollo_aplicaciones.api.model.EntregaResponse;
import com.example.desarrollo_aplicaciones.api.model.FinalizarEntregaResponse;
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

    //PATCH
    //GET PARTICULAR

    @GetMapping("/historial")
    public List<EntregaResponse> obtenerHistorialEntregas() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email).orElse(null);
        Estado estadoFinalizado = estadoRepository.findByNombre(NombreEstado.Finalizado.toString());
        if (user != null && user.getRepartidorId() != null) {
            List<Entrega> entregas = entregaRepository.findByRepartidorIdAndEstadoId(user.getRepartidorId(), estadoFinalizado.getId());
            return entregas.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
        } else {
            return List.of();
        }
    }

    @PatchMapping("/finalizar/{entrega_id}")
    public void finalizarEntrega(@PathVariable Long entrega_id) {

    }

    @GetMapping("/pendientes")
    public List<EntregaResponse> obtenerEntregasPendientes() {
        Estado estadoPendiente = estadoRepository.findByNombre(NombreEstado.Pendiente.toString());
        List<Entrega> entregasPendientes = entregaRepository.findByEstadoIdAndRepartidorIdNull(estadoPendiente.getId());
        return entregasPendientes.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
    }

    @GetMapping("/{entrega_id}")
    public EntregaResponse obtenerEntrega(Long entrega_id) {
        Entrega entrega = entregaRepository.findById(entrega_id).orElse(null);
        return entrega != null ? convertirAEntregaResponse(entrega) : null;
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
        response.setProducto(entrega.getProducto());
        return response;
    }
}
