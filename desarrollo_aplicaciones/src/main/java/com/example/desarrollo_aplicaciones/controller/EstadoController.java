package com.example.desarrollo_aplicaciones.controller;

import com.example.desarrollo_aplicaciones.api.model.EntregaResponse;
import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.Estado;
import com.example.desarrollo_aplicaciones.repository.EstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.desarrollo_aplicaciones.api.model.EstadosResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/estados")
@CrossOrigin(origins = "*")
public class EstadoController {
    @Autowired
    private EstadoRepository estadoRepository;

    @GetMapping
    public List<EstadosResponse> obtenerEstados(){
        List<Estado> estados = estadoRepository.findAll();
        return estados.stream().map(this::convertirAEstadosResponse).collect(Collectors.toList());
    }

    @GetMapping("/{estado_id}")
    public EstadosResponse obtenerEstado(@PathVariable("estado_id") Long estado_id) {
        Estado estado = estadoRepository.findById(estado_id).orElse(null);
        return estado != null ? convertirAEstadosResponse(estado) : null;
    }

    private EstadosResponse convertirAEstadosResponse(Estado estado) {
        EstadosResponse response = new EstadosResponse();
        response.setId(estado.getId());
        response.setNombre(estado.getNombre());
        return response;
    }
}
