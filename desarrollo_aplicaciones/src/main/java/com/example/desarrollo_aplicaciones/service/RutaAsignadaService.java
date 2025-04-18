package com.example.desarrollo_aplicaciones.service;

import java.time.LocalDateTime;
import java.util.Optional;

import com.example.desarrollo_aplicaciones.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.Ruta;
import com.example.desarrollo_aplicaciones.entity.RutaAsignada;
import com.example.desarrollo_aplicaciones.repository.EntregaRepository;
import com.example.desarrollo_aplicaciones.repository.RutaAsignadaRepository;
import com.example.desarrollo_aplicaciones.repository.RutaRepository;

@Service
public class RutaAsignadaService {

    @Autowired
    private RutaAsignadaRepository rutaAsignadaRepository;

    @Autowired
    private EntregaRepository entregaRepository;

    @Autowired
    private EstadoRepository estadoRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Transactional
    public void finalizarRutaAsignada(Long rutaAsignadaId, Long repartidorId) {
        Optional<RutaAsignada> rutaAsignadaOptional = rutaAsignadaRepository.findById(rutaAsignadaId);

        if (rutaAsignadaOptional.isPresent()) {
            RutaAsignada rutaAsignada = rutaAsignadaOptional.get();

            if (!rutaAsignada.getRepartidorId().equals(repartidorId)) {
                throw new RuntimeException("La ruta asignada no pertenece al repartidor.");
            }

            rutaAsignada.setEstado("finalizada");
            rutaAsignada.setFechaFinalizacion(LocalDateTime.now());
            rutaAsignadaRepository.save(rutaAsignada);

            // Crear la Entrega con solo los atributos necesarios
            Entrega entrega = new Entrega();
            entrega.setRepartidorId(rutaAsignada.getRepartidorId());
            entrega.setFechaFinalizacion(LocalDateTime.now());
//            entrega.setEstadoFinal("Finalizada");

            Ruta ruta = rutaAsignada.getRuta();
//            if (ruta != null) {
//                entrega.setCliente(ruta.getCliente());
//            }

            entregaRepository.save(entrega);

        } else {
            throw new RuntimeException("No se encontró la ruta asignada con ID: " + rutaAsignadaId);
        }
    }
}