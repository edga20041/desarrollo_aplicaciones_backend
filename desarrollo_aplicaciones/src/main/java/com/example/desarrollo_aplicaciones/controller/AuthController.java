package com.example.desarrollo_aplicaciones.controller; // Asegúrate de que este paquete coincida con la ubicación de tu clase

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping; // Para encriptar contraseñas
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.desarrollo_aplicaciones.api.model.AuthResponse;
import com.example.desarrollo_aplicaciones.api.model.LoginRequest;
import com.example.desarrollo_aplicaciones.api.model.RegisterRequest;
import com.example.desarrollo_aplicaciones.config.JwtUtil;
import com.example.desarrollo_aplicaciones.entity.User;

import com.example.desarrollo_aplicaciones.repository.UserRepository;

@RestController
@RequestMapping("/auth") // Define la ruta base para este controlador
public class AuthController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
private JwtUtil jwtUtil;
    
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
    
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                // Autenticación exitosa
                String jwtToken = jwtUtil.generateToken(user.getEmail()); // Generar el token JWT
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
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // O podrías devolver un mensaje específico
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

    @GetMapping("/test")
public String testEndpoint() {
    return "Servidor funcionando!";
}
}