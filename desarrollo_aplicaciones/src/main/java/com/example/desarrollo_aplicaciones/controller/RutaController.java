package com.example.desarrollo_aplicaciones.controller;
import com.example.desarrollo_aplicaciones.api.model.RutaResponse;
import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.desarrollo_aplicaciones.entity.Ruta;

@RestController
@RequestMapping("/rutas")
@CrossOrigin(origins = "http://localhost:8000")

//GET PARTICULAR
public class RutaController {

    @Autowired
    private RutaRepository rutaRepository;

    @GetMapping("/{ruta_id}")
    public RutaResponse obtenerRuta(@PathVariable Long ruta_id) {
        Ruta ruta = rutaRepository.findById(ruta_id).orElse(null);
        return ruta != null ? convertirARutaResponse(ruta) : null;
    }

    private RutaResponse convertirARutaResponse(Ruta ruta) {
        RutaResponse response = new RutaResponse();
        response.setId(ruta.getId());
        response.setNombre(ruta.getNombre());
        response.setDescripcion(ruta.getDescripcion());
        response.setOrigen(ruta.getOrigen());
        response.setDestino(ruta.getDestino());
        response.setLatitudOrigen(ruta.getLatitudOrigen());
        response.setLongitudOrigen(ruta.getLongitudOrigen());
        response.setLatitudDestino(ruta.getLatitudDestino());
        response.setLongitudDestino(ruta.getLongitudDestino());
        response.setFechaCreacion(ruta.getFechaCreacion());
        return response;
    }
}