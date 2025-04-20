package com.example.desarrollo_aplicaciones.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

 @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
            .csrf((csrf) -> csrf.disable())
            .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests((authorize) -> authorize
                    .requestMatchers("/auth/register", "/auth/login", "/auth/recover", "/auth/reset-password", "/auth/verify", "/auth/resend-code", "/auth/test", "auth/validate-recovery-code").permitAll()
                    .requestMatchers(HttpMethod.GET, "/rutas/{ruta_id}").authenticated()
                    .requestMatchers(HttpMethod.GET, "/entregas/{entrega_id}").authenticated()
                    .requestMatchers(HttpMethod.GET, "/entregas/historial").authenticated()
                    .requestMatchers(HttpMethod.GET, "/entregas/pendientes").authenticated()
                    .requestMatchers(HttpMethod.PATCH, "/entregas/cambiar_estado").authenticated()
                    .requestMatchers(HttpMethod.GET, "/estados").authenticated()
                    .requestMatchers(HttpMethod.GET, "/estados/{estado_id}").authenticated()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic((httpBasic) -> httpBasic.disable());

    return http.build();
}
}
