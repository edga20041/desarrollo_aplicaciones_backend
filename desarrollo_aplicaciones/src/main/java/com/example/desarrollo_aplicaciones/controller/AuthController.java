package com.example.desarrollo_aplicaciones.controller;

import java.util.Map;
import java.util.Optional;

import com.example.desarrollo_aplicaciones.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.example.desarrollo_aplicaciones.api.model.AuthResponse;
import com.example.desarrollo_aplicaciones.api.model.LoginRequest;
import com.example.desarrollo_aplicaciones.api.model.RegisterRequest;
import com.example.desarrollo_aplicaciones.config.JwtUtil;
import com.example.desarrollo_aplicaciones.entity.User;
import com.example.desarrollo_aplicaciones.repository.UserRepository;
import com.example.desarrollo_aplicaciones.service.PasswordRecoveryService;
import com.example.desarrollo_aplicaciones.entity.PasswordResetToken;
import com.example.desarrollo_aplicaciones.api.model.PasswordResetRequest;

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
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
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

        userRepository.save(newUser);

        String jwtToken = jwtUtil.generateToken(newUser.getEmail());
        AuthResponse response = new AuthResponse(jwtToken, newUser.getId(), newUser.getName());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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
}
