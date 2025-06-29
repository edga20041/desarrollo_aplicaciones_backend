package com.example.desarrollo_aplicaciones.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.desarrollo_aplicaciones.repository.EntregaRepository;

@Service
public class ContadorNotificacionesService {

    private LocalDateTime lastCheck = LocalDateTime.now();

    private final EntregaRepository entregaRepo;

    public ContadorNotificacionesService(EntregaRepository entregaRepo) {
        this.entregaRepo = entregaRepo;
    }

    @Transactional(readOnly = true)
    public synchronized int countAndReset() {
        LocalDateTime now = LocalDateTime.now();
        int count = entregaRepo.countByFechaCreacionAfter(lastCheck);
        lastCheck = now;
        return count;
    }
}
