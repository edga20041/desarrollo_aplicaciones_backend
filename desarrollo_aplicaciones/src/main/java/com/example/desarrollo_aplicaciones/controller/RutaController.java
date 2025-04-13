package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.desarrollo_aplicaciones.entity.Ruta;
import com.example.desarrollo_aplicaciones.entity.RutaAsignada;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.RutaAsignadaRepository;
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
    private GeoCodingService geocodingService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RutaAsignadaRepository rutaAsignadaRepository;

    @GetMapping
    public List<Ruta> obtenerRutas() {
        return rutaRepository.findAll();
    }

    @PostMapping
    public Ruta crearRuta(@RequestBody Ruta nuevaRuta) {
        double[] coordenadas = geocodingService.geocode(nuevaRuta.getDestino());
        nuevaRuta.setLatitud(coordenadas[0]);
        nuevaRuta.setLongitud(coordenadas[1]);
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

        return rutaGuardada;
    }

    @GetMapping("/pendientes")
    public List<Ruta> obtenerRutasPendientesParaRepartidor() {
        Long repartidorId = obtenerRepartidorIdActual();
        if (repartidorId == null) return List.of();

        return rutaAsignadaRepository.findByRepartidorIdAndEstado(repartidorId, "pendiente")
                .stream().map(RutaAsignada::getRuta).toList();
    }

    @PostMapping("/{rutaId}/aceptar")
    public void aceptarRuta(@PathVariable Long rutaId) {
        Long repartidorId = obtenerRepartidorIdActual();
        if (repartidorId == null) return;

        rutaAsignadaRepository.findByRutaIdAndRepartidorId(rutaId, repartidorId).ifPresent(asignada -> {
            asignada.setEstado("aceptada");
            rutaAsignadaRepository.save(asignada);
        });
    }

    @PostMapping("/{rutaId}/rechazar")
    public void rechazarRuta(@PathVariable Long rutaId) {
        Long repartidorId = obtenerRepartidorIdActual();
        if (repartidorId == null) return;

        rutaAsignadaRepository.findByRutaIdAndRepartidorId(rutaId, repartidorId).ifPresent(asignada -> {
            asignada.setEstado("rechazada");
            rutaAsignadaRepository.save(asignada);
        });
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
