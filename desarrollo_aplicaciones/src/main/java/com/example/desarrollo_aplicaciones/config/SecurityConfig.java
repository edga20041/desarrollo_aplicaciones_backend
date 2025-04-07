package com.example.desarrollo_aplicaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .requestMatchers("/auth/register", "/auth/login").permitAll() // Permitir acceso sin autenticación a registro y login
                .requestMatchers("/test").permitAll() // También permitimos /test para pruebas
                .anyRequest().authenticated() // Cualquier otra solicitud requiere autenticación
            )
            .csrf((csrf) -> csrf.disable()) // Deshabilitar CSRF para pruebas de API (¡cuidado en producción!)
            .httpBasic(withDefaults()); // Usar HTTP Basic para la autenticación (puedes cambiarlo a JWT, etc.)
        return http.build();
    }
}