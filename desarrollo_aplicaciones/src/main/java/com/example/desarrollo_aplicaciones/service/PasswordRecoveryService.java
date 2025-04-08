package com.example.desarrollo_aplicaciones.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.desarrollo_aplicaciones.config.JwtUtil;
import com.example.desarrollo_aplicaciones.entity.PasswordResetToken;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.PasswordResetTokenRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class PasswordRecoveryService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${app.frontend.reset-url:http://localhost:4200/reset-password?token=}")
    private String resetUrlBase;

    @Value("${jwt.reset-expiration:1800000}") 
    private long resetExpirationTime;

    @Transactional
    public boolean sendRecoveryEmail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            String token = jwtUtil.generateToken(user.getEmail(), resetExpirationTime); 

            LocalDateTime expiration = LocalDateTime.now().plusMinutes(30);
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setExpirationDate(expiration);
            resetToken.setUser(user);
            tokenRepository.save(resetToken);

            String resetLink = resetUrlBase + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Recuperación de contraseña");
            message.setText("Hola " + user.getName() + ",\n\n" +
                    "Para restablecer tu contraseña, hacé clic en el siguiente enlace:\n" +
                    resetLink + "\n\n" +
                    "Este enlace expirará en " + resetExpirationTime / 60000 + " minutos.\n\n" +
                    "Si no solicitaste esto, ignorá este mensaje.");

            mailSender.send(message);

            return true;
        }

        return false;
    }

    public Optional<User> validateResetToken(String token) {
        if (jwtUtil.isTokenValid(token)) {
            String email = jwtUtil.getSubject(token);
            Optional<User> user = userRepository.findByEmail(email);
            Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);
            if (user.isPresent() && resetToken.isPresent() && resetToken.get().getUser().equals(user.get()) && resetToken.get().getExpirationDate().isAfter(LocalDateTime.now())) {
                return user;
            }
        }
        return Optional.empty();
    }

    public void deleteToken(String token) {
        tokenRepository.findByToken(token).ifPresent(tokenRepository::delete);
    }
}