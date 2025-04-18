package com.example.desarrollo_aplicaciones.controller;
import com.example.desarrollo_aplicaciones.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.desarrollo_aplicaciones.entity.Ruta;

@RestController
@RequestMapping("/rutas")
@CrossOrigin(origins = "http://localhost:8000")

//GET PARTICULAR
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @GetMapping("/{ruta_id}")
    public Ruta obtenerRuta(Long ruta_id) {
        return rutaRepository.findById(ruta_id).orElse(null);
    }
}