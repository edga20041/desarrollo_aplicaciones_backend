package com.example.desarrollo_aplicaciones.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.entity.Ruta;
import com.example.desarrollo_aplicaciones.repository.RutaRepository;
import com.example.desarrollo_aplicaciones.service.GeoCodingService;



@RestController
@RequestMapping("/rutas")
@CrossOrigin(origins = "http://localhost:8000") 
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;
    
    @Autowired
    private GeoCodingService geocodingService;

    @GetMapping
    public List<Ruta> obtenerRutas() {
        return rutaRepository.findAll();
    }

     @PostMapping
    public Ruta crearRuta(@RequestBody Ruta nuevaRuta) {
        double[] coordenadas = geocodingService.geocode(nuevaRuta.getDestino());
        nuevaRuta.setLatitud(coordenadas[0]);
        nuevaRuta.setLongitud(coordenadas[1]);
        return rutaRepository.save(nuevaRuta);
    }
}