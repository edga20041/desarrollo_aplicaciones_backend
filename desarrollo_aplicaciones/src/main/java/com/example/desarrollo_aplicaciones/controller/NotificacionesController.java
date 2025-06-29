// src/main/java/com/example/desarrollo_aplicaciones/controller/NotificacionesController.java
package com.example.desarrollo_aplicaciones.controller;

import com.example.desarrollo_aplicaciones.service.ContadorNotificacionesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionesController {

    private final ContadorNotificacionesService contadorService;

    public NotificacionesController(ContadorNotificacionesService contadorService) {
        this.contadorService = contadorService;
    }

     //Devuelve cuántas entregas nuevas hay (fechaCreacion > última vez)
    @GetMapping("/count")
    public ResponseEntity<Integer> getCountAndReset() {
        int count = contadorService.countAndReset();
        return ResponseEntity.ok(count);
    }
}
