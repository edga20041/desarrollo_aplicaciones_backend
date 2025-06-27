package com.example.desarrollo_aplicaciones.service;

import com.example.desarrollo_aplicaciones.entity.Entrega;
import com.example.desarrollo_aplicaciones.entity.EntregaFinalizacionToken;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.EntregaFinalizacionTokenRepository;
import com.example.desarrollo_aplicaciones.repository.EntregaRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class EntregaFinalizacionService {
    @Autowired
    private EntregaFinalizacionTokenRepository tokenRepository;
    @Autowired
    private EntregaRepository entregaRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JavaMailSender mailSender;

    public final int CODE_LENGTH = 6;
    public final int CODE_EXPIRATION_MINUTES = 10;

    public String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public boolean sendFinalizacionCode(Long entregaId, Long repartidorId) {
        Optional<Entrega> entregaOpt = entregaRepository.findById(entregaId);
        Optional<User> repartidorOpt = userRepository.findById(repartidorId);
        if (entregaOpt.isEmpty() || repartidorOpt.isEmpty()) {
            return false;
        }
        Entrega entrega = entregaOpt.get();
        User repartidor = repartidorOpt.get();

        // Solo un código activo por entrega
        Optional<EntregaFinalizacionToken> existingTokenOpt = tokenRepository.findByEntregaAndRepartidor(entrega, repartidor);
        EntregaFinalizacionToken token;
        String code = generateCode();
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);
        if (existingTokenOpt.isPresent()) {
            token = existingTokenOpt.get();
            token.setToken(code);
            token.setExpirationDate(expirationDate);
        } else {
            token = new EntregaFinalizacionToken();
            token.setEntrega(entrega);
            token.setRepartidor(repartidor);
            token.setToken(code);
            token.setExpirationDate(expirationDate);
        }
        tokenRepository.save(token);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(repartidor.getEmail());
            message.setSubject("Código para finalizar entrega");
            message.setText("Hola " + repartidor.getName() + ",\n\n" +
                    "Tu código para finalizar la entrega (ID: " + entrega.getId() + ") es: " + code + "\n\n" +
                    "Este código expirará en " + CODE_EXPIRATION_MINUTES + " minutos.\n\n" +
                    "Por favor, ingresa este código en la aplicación para finalizar la entrega.\n\n" +
                    "Si no solicitaste esto, ignora este mensaje.");
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
} 