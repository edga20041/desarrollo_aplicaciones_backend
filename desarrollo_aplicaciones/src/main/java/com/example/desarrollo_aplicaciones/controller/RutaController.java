package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.Ruta;
import com.example.desarrollo_aplicaciones.entity.RutaAsignada;
import com.example.desarrollo_aplicaciones.entity.RutaAsignadaConRutaDTO;
import com.example.desarrollo_aplicaciones.entity.RutaRechazada;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.EntregaRepository;
import com.example.desarrollo_aplicaciones.repository.RutaAsignadaRepository;
import com.example.desarrollo_aplicaciones.repository.RutaRechazadaRepository;
import com.example.desarrollo_aplicaciones.repository.RutaRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;
import com.example.desarrollo_aplicaciones.service.GeoCodingService;

@RestController
@RequestMapping("/rutas")
@CrossOrigin(origins = "http://localhost:8000")
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private RutaAsignadaRepository rutaAsignadaRepository;

    @Autowired
    private RutaRechazadaRepository rutaRechazadaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeoCodingService geocodingService;

    @Autowired(required = false)
    private EntregaRepository entregaRepository;

    @GetMapping("/pendientes")
    public ResponseEntity<List<Ruta>> obtenerRutasPendientesParaRepartidor() {
        Long repartidorId = obtenerRepartidorIdActual();
        if (repartidorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RutaAsignada> rutaAsignadaActiva = rutaAsignadaRepository.findByRepartidorIdAndFechaFinalizacionIsNull(repartidorId);
        if (rutaAsignadaActiva.isPresent()) {
            return ResponseEntity.ok(Collections.emptyList()); 
        }
    
        List<Ruta> todasLasRutas = rutaRepository.findAll();
    
        List<Long> rutasAsignadasIds = rutaAsignadaRepository.findAll()
                .stream()
                .map(rutaAsignada -> rutaAsignada.getRuta().getId()) 
                .collect(Collectors.toList());
    
        List<Long> rutasRechazadasIds = rutaRechazadaRepository.findByRepartidorId(repartidorId)
                .stream()
                .map(RutaRechazada::getRutaId)
                .collect(Collectors.toList());
    
        List<Ruta> rutasDisponibles = todasLasRutas.stream()
                .filter(ruta -> !rutasAsignadasIds.contains(ruta.getId())) 
                .filter(ruta -> !rutasRechazadasIds.contains(ruta.getId())) 
                .collect(Collectors.toList());
    
        return ResponseEntity.ok(rutasDisponibles);
    }
    @PostMapping("/aceptar-ruta/{rutaId}")
public ResponseEntity<Void> aceptarRuta(@PathVariable Long rutaId) {
    Long repartidorId = obtenerRepartidorIdActual();
    if (repartidorId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    Optional<RutaAsignada> rutaAsignadaActiva = rutaAsignadaRepository.findByRepartidorIdAndFechaFinalizacionIsNull(repartidorId);
    if (rutaAsignadaActiva.isPresent()) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(null); 
    }

    Optional<Ruta> rutaOptional = rutaRepository.findById(rutaId);
    if (!rutaOptional.isPresent()) {
        return ResponseEntity.notFound().build(); 
    }

    RutaAsignada nuevaAsignacion = new RutaAsignada();
    nuevaAsignacion.setRuta(rutaOptional.get()); 
    nuevaAsignacion.setRepartidorId(repartidorId); 
    nuevaAsignacion.setEstado("aceptada"); 

    rutaAsignadaRepository.save(nuevaAsignacion);

    return ResponseEntity.ok().build(); 
}
    @PostMapping("/{rutaId}/rechazar")
    public ResponseEntity<Void> rechazarRuta(@PathVariable Long rutaId) {
        Long repartidorId = obtenerRepartidorIdActual();
        if (repartidorId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<RutaRechazada> rutaRechazadaOptional = rutaRechazadaRepository
                .findByRutaIdAndRepartidorId(rutaId, repartidorId);

        if (rutaRechazadaOptional.isEmpty()) {
            RutaRechazada rutaRechazada = new RutaRechazada();
            rutaRechazada.setRutaId(rutaId);
            rutaRechazada.setRepartidorId(repartidorId);
            rutaRechazada.setFechaRechazo(LocalDateTime.now());
            rutaRechazadaRepository.save(rutaRechazada);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); 
        }
    }
   @GetMapping("/ruta-asignada")
public ResponseEntity<RutaAsignadaConRutaDTO> obtenerRutaAsignadaActiva() {
    Long repartidorId = obtenerRepartidorIdActual();
    if (repartidorId == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    Optional<RutaAsignada> rutaAsignadaActiva = rutaAsignadaRepository.findByRepartidorIdAndFechaFinalizacionIsNull(repartidorId);
    return rutaAsignadaActiva.map(ra -> new RutaAsignadaConRutaDTO(ra.getId(), ra.getRuta(), ra.getRepartidorId(), ra.getEstado()))
                             .map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
}

      @PostMapping("/finalizar-ruta/{rutaAsignadaId}")
    public ResponseEntity<Void> finalizarRuta(@PathVariable Long rutaAsignadaId) {
        Optional<RutaAsignada> rutaAsignadaOptional = rutaAsignadaRepository.findById(rutaAsignadaId);
        if (!rutaAsignadaOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        RutaAsignada rutaAsignada = rutaAsignadaOptional.get();
        rutaAsignada.setFechaFinalizacion(LocalDateTime.now());
        rutaAsignada.setEstado("finalizada"); 
        rutaAsignadaRepository.save(rutaAsignada);

        if (entregaRepository != null) { 
            Entrega entrega = new Entrega();
            entrega.setRepartidorId(rutaAsignada.getRepartidorId());
            entregaRepository.save(entrega);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Ruta> crearRuta(@RequestBody Ruta nuevaRuta) {
        double[] coordenadasOrigen = geocodingService.geocode(nuevaRuta.getOrigen());
        nuevaRuta.setLatitudOrigen(coordenadasOrigen[0]);
        nuevaRuta.setLongitudOrigen(coordenadasOrigen[1]);

        double[] coordenadasDestino = geocodingService.geocode(nuevaRuta.getDestino());
        nuevaRuta.setLatitudDestino(coordenadasDestino[0]);
        nuevaRuta.setLongitudDestino(coordenadasDestino[1]);

        nuevaRuta.setFechaCreacion(LocalDateTime.now());
        Ruta rutaGuardada = rutaRepository.save(nuevaRuta);

        List<User> repartidores = userRepository.findAll().stream()
                .filter(u -> u.getRepartidorId() != null)
                .toList();

        for (User repartidor : repartidores) {
            RutaAsignada asignacion = new RutaAsignada();
            asignacion.setRuta(rutaGuardada);
            asignacion.setRepartidorId(repartidor.getRepartidorId());
            asignacion.setEstado("pendiente");
            rutaAsignadaRepository.save(asignacion);
        }

        return new ResponseEntity<>(rutaGuardada, HttpStatus.CREATED);
    }

    private Long obtenerRepartidorIdActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = (principal instanceof UserDetails)
                ? ((UserDetails) principal).getUsername()
                : principal.toString();

        return userRepository.findByEmail(email)
                .map(User::getRepartidorId)
                .orElse(null);
    }
}