package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
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
import com.example.desarrollo_aplicaciones.service.EntregaFinalizacionService;
import com.example.desarrollo_aplicaciones.entity.EntregaFinalizacionToken;
import com.example.desarrollo_aplicaciones.repository.EntregaFinalizacionTokenRepository;
import org.springframework.http.ResponseEntity;
import com.example.desarrollo_aplicaciones.api.model.VerificarCodigoRequest;

@RestController
@RequestMapping("/entregas")
@CrossOrigin(origins = "*")
public class EntregaController {

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private EntregaFinalizacionService entregaFinalizacionService;

    @Autowired
    private EntregaFinalizacionTokenRepository entregaFinalizacionTokenRepository;

    //PATCH
    //GET PARTICULAR    

    @GetMapping("/historial")
    public List<EntregaResponse> obtenerHistorialEntregas() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null && user.getRepartidorId() != null) {
            List<Entrega> entregas = entregaRepository.findByRepartidorIdAndEstadoId(user.getRepartidorId(), 3L);
            
            return entregas.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
        } else {
            return List.of(); 
        }
    }


    @PatchMapping("/cambiar_estado")
    public CambiarEstadoEntregaResponse cambiarEstadoEntrega(@RequestBody CambiarEstadoEntregaRequest request) {
        Long entregaId = request.getEntregaId();
        Long estadoId = request.getEstadoId();
        Long repartidorIdRequest = request.getRepartidorId();
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        Estado estado = estadoRepository.findById(estadoId).orElse(null);

        if (estado == null | entrega == null) {
            return cambiarEstadoEntregaResponse("Error", "Estado y/o entrega no encontrados");
        }

        // Obtener el usuario autenticado (asumiendo que el repartidor está autenticado)
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        User usuarioAutenticado = userRepository.findByEmail(email).orElse(null);

        if (estado.getNombre().equals(NombreEstado.EnProceso.toString())) {
            entrega.setEstadoId(estadoId);
            entrega.setFechaAsignacion(LocalDateTime.now());
            entrega.setFechaFinalizacion(null);

            if (repartidorIdRequest == null) {
                return cambiarEstadoEntregaResponse("Error", "El repartidor no puede ser nulo en el estado En Proceso");
            }
            User repartidor = userRepository.findById(repartidorIdRequest).orElse(null);
            if (repartidor == null) {
                return cambiarEstadoEntregaResponse("Error", "El repartidor no fue encontrado en el sistema");
            }
            entrega.setRepartidorId(repartidor.getId());
            entregaRepository.save(entrega);
            return cambiarEstadoEntregaResponse("Ok", "Estado de entrega cambiado correctamente");
        } else if (estado.getNombre().equals(NombreEstado.Finalizado.toString())) {
            // En vez de finalizar, generamos y enviamos el código al repartidor
            Long repartidorId = (usuarioAutenticado != null && usuarioAutenticado.getRepartidorId() != null)
                ? usuarioAutenticado.getRepartidorId()
                : repartidorIdRequest;
            if (repartidorId == null) {
                return cambiarEstadoEntregaResponse("Error", "No se pudo determinar el repartidor para enviar el código");
            }
            boolean enviado = entregaFinalizacionService.sendFinalizacionCode(entregaId, repartidorId);
            if (enviado) {
                return cambiarEstadoEntregaResponse("Ok", "Se envió el código de finalización al repartidor. Debe ingresarlo para finalizar la entrega.");
            } else {
                return cambiarEstadoEntregaResponse("Error", "No se pudo enviar el código de finalización por email");
            }
        } else if (estado.getNombre().equals(NombreEstado.Pendiente.toString())) {
            entrega.setEstadoId(estadoId);
            entrega.setRepartidorId(null);
            entrega.setFechaAsignacion(null);
            entrega.setFechaFinalizacion(null);
            entregaRepository.save(entrega);
            return cambiarEstadoEntregaResponse("Ok", "Estado de entrega cambiado correctamente");
        }
        return cambiarEstadoEntregaResponse("Error", "Estado no soportado");
    }

    @GetMapping("/pendientes")
    public List<EntregaResponse> obtenerEntregasPendientes() {
        Estado estadoPendiente = estadoRepository.findByNombre(NombreEstado.Pendiente.toString());
        List<Entrega> entregasPendientes = entregaRepository.findByEstadoIdAndRepartidorIdNull(estadoPendiente.getId());
        return entregasPendientes.stream().map(this::convertirAEntregaResponse).collect(Collectors.toList());
    }

    @GetMapping("/{entrega_id}")
    public EntregaResponse obtenerEntrega(@PathVariable("entrega_id") Long entregaId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        return entrega != null ? convertirAEntregaResponse(entrega) : null;
    }

    @GetMapping("/token_finalizacion")
    public CambiarEstadoEntregaResponse obtenerTokenFinalizacion(@RequestParam Long entregaId, @RequestParam Long repartidorId) {
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        User repartidor = userRepository.findById(repartidorId).orElse(null);
        if (entrega == null || repartidor == null) {
            return cambiarEstadoEntregaResponse("Error", "Entrega o repartidor no encontrados");
        }
        EntregaFinalizacionToken token = entregaFinalizacionTokenRepository.findByEntregaAndRepartidor(entrega, repartidor).orElse(null);
        if (token == null) {
            return cambiarEstadoEntregaResponse("Error", "No existe token para esta entrega y repartidor");
        }
        if (token.getExpirationDate().isBefore(java.time.LocalDateTime.now())) {
            return cambiarEstadoEntregaResponse("Error", "El token ha expirado");
        }
        CambiarEstadoEntregaResponse response = new CambiarEstadoEntregaResponse();
        response.setStatus("Ok");
        response.setMessage(token.getToken());
        return response;
    }

    @PostMapping("/verificar_codigo")
    public CambiarEstadoEntregaResponse verificarCodigoFinalizacion(@RequestBody VerificarCodigoRequest request) {
        Long entregaId = request.getEntregaId();
        Long repartidorId = request.getRepartidorId();
        String codigo = request.getCodigo();
        Entrega entrega = entregaRepository.findById(entregaId).orElse(null);
        User repartidor = userRepository.findById(repartidorId).orElse(null);
        if (entrega == null || repartidor == null) {
            return cambiarEstadoEntregaResponse("Error", "Entrega o repartidor no encontrados");
        }
        EntregaFinalizacionToken token = entregaFinalizacionTokenRepository.findByEntregaAndRepartidor(entrega, repartidor).orElse(null);
        if (token == null) {
            return cambiarEstadoEntregaResponse("Error", "No existe token para esta entrega y repartidor");
        }
        if (token.getExpirationDate().isBefore(LocalDateTime.now())) {
            return cambiarEstadoEntregaResponse("Error", "El código ha expirado");
        }
        if (!token.getToken().equals(codigo)) {
            return cambiarEstadoEntregaResponse("Error", "El código es incorrecto");
        }
        // Cambiar estado a Finalizado y guardar fecha de finalización
        entrega.setEstadoId(/* ID del estado Finalizado */ 3L);
        entrega.setFechaFinalizacion(LocalDateTime.now());
        entregaRepository.save(entrega);
        // (Opcional) eliminar el token usado
        entregaFinalizacionTokenRepository.delete(token);
        return cambiarEstadoEntregaResponse("Ok", "Entrega finalizada correctamente");
    }

    @GetMapping("/en-progreso")
    public EntregaResponse obtenerEntregaEnProgreso() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null && user.getRepartidorId() != null) {
            // Obtener la entrega en progreso (estadoId = 2) del repartidor
            List<Entrega> entregasEnProgreso = entregaRepository.findByRepartidorIdAndEstadoId(user.getRepartidorId(), 2L);
            
            if (!entregasEnProgreso.isEmpty()) {
                return convertirAEntregaResponse(entregasEnProgreso.get(0));
            }
        }
        
        return null; // No hay entrega en progreso
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
