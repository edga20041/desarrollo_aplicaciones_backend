package com.example.desarrollo_aplicaciones.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.desarrollo_aplicaciones.api.model.CambiarEstadoEntregaRequest;
import com.example.desarrollo_aplicaciones.entity.Estado;
import com.example.desarrollo_aplicaciones.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.desarrollo_aplicaciones.api.model.EntregaResponse;
import com.example.desarrollo_aplicaciones.api.model.CambiarEstadoEntregaResponse;
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
        
        if (user != null && user.getRepartidorId() != null) {
            List<Entrega> entregas = entregaRepository.findByRepartidorId(user.getRepartidorId());
            
            // Convertir las entregas a la respuesta que queremos devolver
            return entregas.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
        } else {
            return List.of(); // Retorna una lista vacía si no se encuentra el usuario o no tiene repartidorId
        }
    }


    @PatchMapping("/cambiar_estado")
    public CambiarEstadoEntregaResponse cambiarEstadoEntrega(@RequestBody CambiarEstadoEntregaRequest request) {
        Long entregaId = request.getEntregaId();
        Long estadoId = request.getEstadoId();
        Long repartidorIdRequest = request.getRepartidorId(); // Obtenemos el repartidorId de la request (puede ser null)
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        Estado estado = estadoRepository.findById(estadoId).orElse(null);
    
        if (estado == null | entrega == null) {
            return cambiarEstadoEntregaResponse("Error", "Estado y/o entrega no encontrados");
        }
    
        entrega.setEstadoId(estadoId);
    
        // Obtener el usuario autenticado (asumiendo que el repartidor está autenticado)
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        User usuarioAutenticado = userRepository.findByEmail(email).orElse(null);
    
        if (estado.getNombre().equals(NombreEstado.EnProceso.toString())) {
            if (repartidorIdRequest == null) {
                return cambiarEstadoEntregaResponse("Error", "El repartidor no puede ser nulo en el estado En Proceso");
            }
            User repartidor = userRepository.findById(repartidorIdRequest).orElse(null);
            if (repartidor == null) {
                return cambiarEstadoEntregaResponse("Error", "El repartidor no fue encontrado en el sistema");
            }
            entrega.setRepartidorId(repartidor.getId());
        } else if (estado.getNombre().equals(NombreEstado.Finalizado.toString())) {
            // **Guardar el ID del usuario autenticado como el repartidor que finalizó**
            if (usuarioAutenticado != null && usuarioAutenticado.getRepartidorId() != null) {
                entrega.setRepartidorId(usuarioAutenticado.getRepartidorId());
            } else if (repartidorIdRequest != null) {
                // Si por alguna razón se envía un repartidorId en la request, también lo guardamos
                User repartidor = userRepository.findById(repartidorIdRequest).orElse(null);
                if (repartidor != null) {
                    entrega.setRepartidorId(repartidor.getId());
                }
            } else {
                entrega.setRepartidorId(null); // Si no hay usuario autenticado con repartidorId ni se envía en la request
            }
        } else if (estado.getNombre().equals(NombreEstado.Pendiente.toString())) {
            entrega.setRepartidorId(null);
        }
    
        entregaRepository.save(entrega);
        return cambiarEstadoEntregaResponse("Ok", "Estado de entrega cambiado correctamente");
    }

    @GetMapping("/pendientes")
    public List<EntregaResponse> obtenerEntregasPendientes() {
        Estado estadoPendiente = estadoRepository.findByNombre(NombreEstado.Pendiente.toString());
        List<Entrega> entregasPendientes = entregaRepository.findByEstadoIdAndRepartidorIdNull(estadoPendiente.getId());
        return entregasPendientes.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
    }

    @GetMapping("/{entrega_id}")
    public EntregaResponse obtenerEntrega(@PathVariable Long entrega_id) {
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

    private CambiarEstadoEntregaResponse cambiarEstadoEntregaResponse(String status, String message) {
        CambiarEstadoEntregaResponse response = new CambiarEstadoEntregaResponse();
        response.setMessage(message);
        response.setStatus(status);
        return response;
    }
}
