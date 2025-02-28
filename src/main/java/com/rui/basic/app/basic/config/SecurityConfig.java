package com.rui.basic.app.basic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.rui.basic.app.basic.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LegacyPasswordEncoder passwordEncoder;

    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         LegacyPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/img/**", "/error", "/api/test/**").permitAll()
                
                // Endpoints para observaciones - Accesibles para todos los autenticados
                .requestMatchers("/intermediary/field-observation/**", 
                                "/intermediary/fields-with-observations/**", 
                                "/intermediary/idoneidad-has-observation/**", 
                                "/intermediary/workexp-has-observation/**").permitAll()
                
                // Endpoints específicos para roles
                .requestMatchers("/api/idoneidad/**").hasRole("Soporte")
                .requestMatchers("/api/intermediary/**").hasRole("Soporte")
                
                // Rutas de intermediario
                .requestMatchers("/intermediary/my-registries").hasAnyRole("Intermediario", "Soporte", "ADMIN")
                .requestMatchers("/intermediary/complement/**").hasAnyRole("Intermediario", "Soporte", "ADMIN")
                .requestMatchers("/intermediary/edit-intermediary").hasAnyRole("Intermediario", "Soporte", "ADMIN")
                
                // Dashboard accesible para todos los autenticados
                .requestMatchers("/dashboard").authenticated()
                
                // Cualquier otra solicitud requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/auth/login?error=true")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
}