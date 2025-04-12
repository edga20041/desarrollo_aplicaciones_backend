package com.example.desarrollo_aplicaciones.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.entity.Ruta;
import com.example.desarrollo_aplicaciones.repository.RutaRepository;

@RestController
@RequestMapping("/rutas")
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @GetMapping
    public List<Ruta> obtenerRutas() {
        return rutaRepository.findAll();
    }
}