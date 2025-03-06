package com.rui.basic.app.basic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.rui.basic.app.basic.service.CustomUserDetailsService;

import jakarta.servlet.ServletContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final LegacyPasswordEncoder passwordEncoder;

    @Value("${ad.url}")
    private String ldapUrl;

    @Value("${ad.domain}")
    private String ldapDomain;

    @Value("${ad.searchBase}")
    private String ldapSearchBase;

    @Value("${ad.security.authentication}")
    private String ldapAuthentication;

    public SecurityConfig(CustomUserDetailsService userDetailsService, 
                         LegacyPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Mantener CSRF habilitado (por seguridad)
            .csrf(csrf -> csrf
                // Configurar CSRF para permitir la operación de logout
                .ignoringRequestMatchers("/logout"))
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
                .requestMatchers("/intermediary/my-registries").hasAnyRole("Intermediario", "Soporte", "Administrador")
                .requestMatchers("/intermediary/complement/**").hasAnyRole("Intermediario", "Soporte", "Administrador")
                .requestMatchers("/intermediary/edit-intermediary").hasAnyRole("Intermediario", "Soporte", "Administrador")
                
                // Lista de intermediarios restringida para Intermediario
                .requestMatchers("/intermediary/list").hasAnyRole("Soporte", "Funcionario","Administrador","Funcionario2","Aprobador")

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
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .permitAll()
                // Permitir logout con GET (además del POST por defecto)
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
            )
            // Control de sesiones máximas
            .sessionManagement(session -> session
                //.invalidSessionUrl("/auth/login?invalid=true")
                // Configuración para prevenir ataques de fijación de sesión
                .sessionFixation().migrateSession()
                // Política de creación de sesiones
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                // Control de sesiones concurrentes
                .maximumSessions(1)                          // Máximo 1 sesión por usuario
                .maxSessionsPreventsLogin(false)             // Permitir nuevos inicios y expulsar la sesión anterior
                .expiredUrl("/auth/login?expired=true")      // Redirección cuando una sesión es invalidada
                .sessionRegistry(sessionRegistry())          // Registro de sesiones
            )
            // Manejo de acceso denegado
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/dashboard");
                })
            );

        return http.build();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // Configuración de tiempo de expiración de la sesión (10 minutos)
    @Bean
    public jakarta.servlet.http.HttpSessionListener httpSessionListener() {
        return new jakarta.servlet.http.HttpSessionListener() {
            @Override
            public void sessionCreated(jakarta.servlet.http.HttpSessionEvent event) {
                event.getSession().setMaxInactiveInterval(600); // 10 minutos en segundos
            }
            
            @Override
            public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent event) {
                // No se requiere ninguna acción específica
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder auth = http.getSharedObject(AuthenticationManagerBuilder.class);

        // Proveedor de autenticación LDAP (Active Directory)
        ActiveDirectoryLdapAuthenticationProvider ldapProvider = 
            new ActiveDirectoryLdapAuthenticationProvider(ldapDomain, ldapUrl);
        ldapProvider.setSearchFilter("(&(objectClass=user)(sAMAccountName={0}))");
        ldapProvider.setUserDetailsContextMapper(new CustomUserDetailsContextMapper(userDetailsService));

        // Proveedor de autenticación de base de datos como respaldo
        DaoAuthenticationProvider dbProvider = authenticationProvider();

        // Configurar ambos proveedores (LDAP primero, luego DB como respaldo)
        auth.authenticationProvider(ldapProvider)
            .authenticationProvider(dbProvider);

        return auth.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    // Clase interna para mapear detalles del usuario desde LDAP
    private static class CustomUserDetailsContextMapper implements UserDetailsContextMapper {
        private final CustomUserDetailsService userDetailsService;

        public CustomUserDetailsContextMapper(CustomUserDetailsService userDetailsService) {
            this.userDetailsService = userDetailsService;
        }

        @Override
        public UserDetails mapUserFromContext(
                org.springframework.ldap.core.DirContextOperations ctx, 
                String username, 
                java.util.Collection<? extends GrantedAuthority> authorities) {
            // Delegar al CustomUserDetailsService para obtener detalles y roles desde la DB
            return userDetailsService.loadUserByUsername(username);
        }

        @Override
        public void mapUserToContext(
                UserDetails user, 
                org.springframework.ldap.core.DirContextAdapter ctx) {
        }
    }
}