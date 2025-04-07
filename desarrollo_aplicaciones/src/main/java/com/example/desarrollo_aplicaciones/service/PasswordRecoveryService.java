package com.example.desarrollo_aplicaciones.service;

import com.example.desarrollo_aplicaciones.entity.PasswordResetToken;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.PasswordResetTokenRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordRecoveryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.reset-url:http://localhost:4200/reset-password?token=}")
    private String resetUrlBase;

    @Transactional
    public boolean sendRecoveryEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 1. Generar token
            String token = UUID.randomUUID().toString();
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(30);

            // 2. Guardar en base de datos
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setExpirationDate(expiration);
            resetToken.setUser(user);
            tokenRepository.save(resetToken);

            // 3. Enviar email
            String resetLink = resetUrlBase + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Recuperación de contraseña");
            message.setText("Hola " + user.getName() + ",\n\n" +
                    "Para restablecer tu contraseña, hacé clic en el siguiente enlace:\n" +
                    resetLink + "\n\n" +
                    "Este enlace expirará en 30 minutos.\n\n" +
                    "Si no solicitaste esto, ignorá este mensaje.");

            mailSender.send(message);

            return true;
        }

        return false;
    }
}

