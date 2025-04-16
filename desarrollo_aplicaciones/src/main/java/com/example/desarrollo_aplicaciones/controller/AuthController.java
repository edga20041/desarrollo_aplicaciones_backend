package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.api.model.AuthResponse;
import com.example.desarrollo_aplicaciones.api.model.LoginRequest;
import com.example.desarrollo_aplicaciones.api.model.PasswordResetRequest;
import com.example.desarrollo_aplicaciones.api.model.RegisterRequest;
import com.example.desarrollo_aplicaciones.api.model.UserResponse;
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
@CrossOrigin(origins = "http://localhost:8000") 

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
                return new ResponseEntity<>("El usuario ya está registrado.", HttpStatus.BAD_REQUEST);
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

            newUser.setRepartidorId(newUser.getId());
            userRepository.save(newUser);

            String code = String.format("%06d", (int)(Math.random() * 1000000));
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setCode(code);
            verificationToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));
            verificationToken.setUser(newUser);
            verificationTokenRepository.save(verificationToken);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(newUser.getEmail());
            message.setSubject("Código de verificación");
            message.setText("Hola " + newUser.getName() + ",\n\n" +
                    "Tu código de verificación es: " + code + "\n\n" +
                    "Este código expirará en 10 minutos.\n\n" +
                    "Gracias.");
            mailSender.send(message);

            return ResponseEntity.ok("Registro iniciado. Código enviado al correo.");
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
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Token expirado."));
                }

                User user = token.getUser();
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(user);

                passwordResetTokenRepository.delete(token);

                return ResponseEntity.ok(Map.of("message", "Contraseña restablecida correctamente."));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Token inválido."));
            }
        }

        @GetMapping("/test")
        public String testEndpoint() {
            return "Servidor funcionando!";
        }

        @PostMapping("/verify")
        public ResponseEntity<AuthResponse> verifyEmail(@RequestBody Map<String, String> body) {
            String code = body.get("code");

            Optional<VerificationToken> tokenOptional = verificationTokenRepository.findByCode(code);

            if (tokenOptional.isPresent()) {
                VerificationToken verificationToken = tokenOptional.get();

                if (verificationToken.getExpirationDate().isBefore(LocalDateTime.now())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                         .body(null);
                }

                User user = verificationToken.getUser();
                user.setEnabled(true);
                userRepository.save(user);

                verificationTokenRepository.delete(verificationToken);

                String jwt = jwtUtil.generateToken(user.getEmail());
                AuthResponse authResponse = new AuthResponse(jwt, user.getId(), user.getName());

                return ResponseEntity.ok(authResponse);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }


        @PostMapping("/resend-code")
    public ResponseEntity<?> resendVerificationCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }

        User user = userOptional.get();

        if (user.isEnabled()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El usuario ya está verificado.");
        }

        Optional<VerificationToken> tokenOptional = verificationTokenRepository.findByUser(user);
        VerificationToken verificationToken;

        if (tokenOptional.isPresent()) {
            verificationToken = tokenOptional.get();
        } else {
            verificationToken = new VerificationToken();
            verificationToken.setUser(user);
        }

        // Generar nuevo código
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        verificationToken.setCode(code);
        verificationToken.setExpirationDate(LocalDateTime.now().plusMinutes(10));

        verificationTokenRepository.save(verificationToken);

        // Reenviar correo
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reenvío de código de verificación");
        message.setText("Hola " + user.getName() + ",\n\n" +
                "Tu nuevo código de verificación es: " + code + "\n\n" +
                "Este código expirará en 10 minutos.\n\n" +
                "Gracias.");
        mailSender.send(message);

        return ResponseEntity.ok("Código reenviado con éxito.");
    }
    @GetMapping("/user/me")
    public ResponseEntity<UserResponse> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String email = userDetails.getUsername();

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserResponse userResponse = new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getSurname(),
                        user.getEmail(),
                        user.getPhoneNumber(),
                        user.getDni()
                );
                return new ResponseEntity<>(userResponse, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    private boolean isValidName(String name) {
        return name.matches("^[A-Z][a-zA-Z]*$");
    }

    private boolean isValidSurname(String surname) {
        return surname.matches("^[A-Z][a-zA-Z]*$");
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d{9}$");
    }

    private boolean isValidDni(Integer dni) {
        if (dni == null) return false;
        String dniStr = String.valueOf(dni);
        return dniStr.matches("^\\d{8}$");
    }

    private boolean isValidEmail(String email) {
        return email.contains("@");
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[A-Z])(?=.*\\d).{9,}$");
    }

}
