package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.api.model.AuthResponse;
import com.example.desarrollo_aplicaciones.api.model.LoginRequest;
import com.example.desarrollo_aplicaciones.api.model.PasswordResetRequest;
import com.example.desarrollo_aplicaciones.api.model.RegisterRequest;
import com.example.desarrollo_aplicaciones.config.JwtUtil;
import com.example.desarrollo_aplicaciones.entity.PasswordResetToken;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.entity.VerificationToken;
import com.example.desarrollo_aplicaciones.repository.PasswordResetTokenRepository;
import com.example.desarrollo_aplicaciones.repository.UserRepository;
import com.example.desarrollo_aplicaciones.repository.VerificationTokenRepository;
import com.example.desarrollo_aplicaciones.service.PasswordRecoveryService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordRecoveryService passwordRecoveryService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

     @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                String jwtToken = jwtUtil.generateToken(user.getEmail());
                AuthResponse response = new AuthResponse(jwtToken, user.getId(), user.getName());
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setName(registerRequest.getName());
        newUser.setSurname(registerRequest.getSurname());
        newUser.setPhoneNumber(registerRequest.getPhoneNumber());
        newUser.setDni(registerRequest.getDni());

        String encryptedPassword = passwordEncoder.encode(registerRequest.getPassword());
        newUser.setPassword(encryptedPassword);
        newUser.setEnabled(false);

        userRepository.save(newUser);

        // Generar token de verificación
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setExpirationDate(LocalDateTime.now().plusHours(24)); // Expira en 24 horas
        verificationToken.setUser(newUser);
        verificationTokenRepository.save(verificationToken);

        // Enviar correo de verificación
        String verificationLink = "http://localhost:8081/auth/verify?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(newUser.getEmail());
        message.setSubject("Verificación de correo electrónico");
        message.setText("Hola " + newUser.getName() + ",\n\n" +
                "Por favor, verifica tu correo electrónico haciendo clic en el siguiente enlace:\n" +
                verificationLink + "\n\n" +
                "Este enlace expirará en 24 horas.\n\n" +
                "Gracias.");
        mailSender.send(message);

    return ResponseEntity.ok("Registro iniciado. Por favor, verifica tu correo electrónico.");
    }

    @PostMapping("/recover")
    public ResponseEntity<?> sendRecoveryEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        boolean enviado = passwordRecoveryService.sendRecoveryEmail(email);
        if (enviado) {
            return ResponseEntity.ok("Email de recuperación enviado.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody PasswordResetRequest request) {
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(request.getToken());

        if (tokenOptional.isPresent()) {
            PasswordResetToken token = tokenOptional.get();

            if (token.getExpirationDate().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token expirado.");
            }

            User user = token.getUser();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            passwordResetTokenRepository.delete(token);

            return ResponseEntity.ok("Contraseña restablecida correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Token inválido.");
        }
    }

    @GetMapping("/test")
    public String testEndpoint() {
        return "Servidor funcionando!";
    }

    @GetMapping("/verify")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam("token") String token) {
        Optional<VerificationToken> tokenOptional = verificationTokenRepository.findByToken(token);
    
        if (tokenOptional.isPresent()) {
            VerificationToken verificationToken = tokenOptional.get();
    
            // Verificar si el token ha expirado
            if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
    
            // Activar el usuario
            User user = verificationToken.getUser();
            user.setEnabled(true);
            userRepository.save(user);
    
            // Eliminar el token de verificación
            verificationTokenRepository.delete(verificationToken);
    
            // Generar AuthResponse
            String jwtToken = jwtUtil.generateToken(user.getEmail());
            AuthResponse authResponse = new AuthResponse(jwtToken, user.getId(), user.getName());
    
            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
