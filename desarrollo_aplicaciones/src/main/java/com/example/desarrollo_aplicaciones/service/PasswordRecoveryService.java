package com.example.desarrollo_aplicaciones.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.desarrollo_aplicaciones.entity.PasswordResetToken;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.PasswordResetTokenRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PasswordRecoveryService {

    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    public final int CODE_LENGTH = 6; 
    public final int CODE_EXPIRATION_MINUTES = 10; 

    public String generateVerificationCode() { 
        Random random = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
    @Transactional
    public boolean sendRecoveryEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            String code = generateVerificationCode();
            LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(CODE_EXPIRATION_MINUTES);

            Optional<PasswordResetToken> existingTokenOptional = tokenRepository.findByUser(user);
            PasswordResetToken resetToken;

            if (existingTokenOptional.isPresent()) {
                resetToken = existingTokenOptional.get();
                resetToken.setToken(code);
                resetToken.setExpirationDate(expirationDate);
            } else {
                resetToken = new PasswordResetToken();
                resetToken.setToken(code);
                resetToken.setExpirationDate(expirationDate);
                resetToken.setUser(user);
            }

            try {
                tokenRepository.save(resetToken);

                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("Código de recuperación de contraseña");
                message.setText("Hola " + user.getName() + ",\n\n" +
                        "Tu código de recuperación es: " + code + "\n\n" +
                        "Este código expirará en " + CODE_EXPIRATION_MINUTES + " minutos.\n\n" +
                        "Por favor, ingresa este código en la aplicación para continuar con el restablecimiento de tu contraseña.\n\n" +
                        "Si no solicitaste esto, ignora este mensaje.");
                mailSender.send(message);
                return true;
            } catch (Exception e) {
                logger.error("Error al guardar el token de recuperación para el usuario {}: {}", user.getId(), e.getMessage(), e);
                return false;
            }
        }
        return false;
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    @Transactional
    public void deleteToken(PasswordResetToken token) {
        tokenRepository.delete(token);
    }
}