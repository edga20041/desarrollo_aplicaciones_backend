package com.example.desarrollo_aplicaciones.controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.example.desarrollo_aplicaciones.helpers.Validations.*;


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:8000","http://192.168.1.10:8081","exp://192.168.0.186:8082","http://localhost:8082"}) 

public class AuthController {
    @Autowired
    private UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

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

        // se puede agregar formateo previo a validacion, ahora solo se hace en la app

        if (!registerValidate(registerRequest)) {
            return new ResponseEntity<>("Los campos no pasaron las validaciones.", HttpStatus.BAD_REQUEST);
        }
        User newUser = createUserEntity(registerRequest);

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

    private boolean registerValidate(RegisterRequest registerRequest){
        return (isValidName(registerRequest.getName())| isValidName(registerRequest.getSurname()) | isValidDni(registerRequest.getDni()) | isValidEmail(registerRequest.getEmail()) | isValidDni(registerRequest.getDni()) | isValidPassword(registerRequest.getPassword()) | isValidPhoneNumber(registerRequest.getPhoneNumber(), "AR"));
    }

    private User createUserEntity(RegisterRequest registerRequest){
        User user = new User();

        user.setEmail(registerRequest.getEmail());
        user.setName(registerRequest.getName());
        user.setSurname(registerRequest.getSurname());
        user.setPhoneNumber(registerRequest.getPhoneNumber());
        user.setDni(registerRequest.getDni());
        String encryptedPassword = passwordEncoder.encode(registerRequest.getPassword());
        user.setPassword(encryptedPassword);
        user.setEnabled(false);

        return user;
    }
    @PostMapping("/validate-recovery-code")
public ResponseEntity<?> validateRecoveryCode(@RequestBody Map<String, String> payload) {
    String email = payload.get("email");
    String code = payload.get("code");

    logger.info("Validando código para el email: {}, código recibido: {}", email, code); // Añade este log

    Optional<User> userOptional = userRepository.findByEmail(email);
    if (userOptional.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Correo electrónico no encontrado."));
    }

    User user = userOptional.get();
    Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByUserAndToken(user, code);

    logger.info("Resultado de la búsqueda del token: {}", tokenOptional.isPresent()); // Añade este log

    if (tokenOptional.isPresent() && tokenOptional.get().getExpirationDate().isAfter(LocalDateTime.now())) {
        // Código válido
        return ResponseEntity.ok(Map.of("message", "Código válido", "token", tokenOptional.get().getToken()));
    } else {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Código inválido o expirado."));
    }

    
}

@PostMapping("/resend-recovery-code")
    public ResponseEntity<?> resendRecoveryCode(@RequestBody Map<String, String> body) {
        String email = body.get("email");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Usuario no encontrado."));
        }

        User user = userOptional.get();

        // Generar un nuevo código de recuperación
        String code = passwordRecoveryService.generateVerificationCode(); // Reutiliza tu método de generación de código
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(passwordRecoveryService.CODE_EXPIRATION_MINUTES);

        // Actualizar o crear un nuevo token de recuperación en la base de datos
        Optional<PasswordResetToken> existingTokenOptional = passwordResetTokenRepository.findByUser(user);
        PasswordResetToken resetToken;

        if (existingTokenOptional.isPresent()) {
            resetToken = existingTokenOptional.get();
            resetToken.setToken(code); // Actualizar el token con el nuevo código
            resetToken.setExpirationDate(expirationDate);
        } else {
            resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(code);
            resetToken.setExpirationDate(expirationDate);
        }

        passwordResetTokenRepository.save(resetToken);

        // Enviar el nuevo código por correo electrónico
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reenvío de código de recuperación de contraseña");
        message.setText("Hola " + user.getName() + ",\n\n" +
                "Tu nuevo código de recuperación es: " + code + "\n\n" +
                "Este código expirará en " + passwordRecoveryService.CODE_EXPIRATION_MINUTES + " minutos.\n\n" +
                "Por favor, ingresa este código en la aplicación para continuar con el restablecimiento de tu contraseña.\n\n" +
                "Si no solicitaste esto, ignora este mensaje.");
        mailSender.send(message);

        return ResponseEntity.ok(Map.of("message", "Nuevo código de recuperación enviado."));
    }
}
