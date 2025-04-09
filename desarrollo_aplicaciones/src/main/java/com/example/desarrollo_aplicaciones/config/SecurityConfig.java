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
                .requestMatchers("/auth/register", "/auth/login", "/auth/recover", "/auth/reset-password","/auth/verify","/auth/resend-code").permitAll() // Permitir acceso sin autenticación a estos endpoints
                .requestMatchers("auth/test").permitAll()
                .anyRequest().authenticated()
            )
            .csrf((csrf) -> csrf.disable())
            .httpBasic(withDefaults());
        return http.build();
    }
}