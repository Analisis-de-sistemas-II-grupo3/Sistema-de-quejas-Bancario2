package com.umg.quejasbancario.config;

import com.umg.quejasbancario.security.CustomUserDetailsService;
import com.umg.quejasbancario.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Configuracion de seguridad. El acceso a los modulos esta restringido segun
 * el rol del usuario autenticado (RNF05, RN01). Ademas de estas reglas
 * generales por ruta, los controladores usan @PreAuthorize para reglas mas
 * finas (p.ej. un Cliente solo puede ver sus propios casos, CU-03 FA02).
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final AppProperties appProperties;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Publico: autenticacion, recuperacion de contrasena
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/catalogos/**").authenticated()
                .requestMatchers("/uploads/**").authenticated()

                // Cliente / Denunciante (RN01, CU-02, CU-03)
                .requestMatchers("/api/casos/registrar", "/api/casos/mis-casos/**").hasRole("CLIENTE")

                // Agente de Atencion (CU-05, CU-07, CU-08, CU-09)
                .requestMatchers("/api/casos/bandeja/**").hasRole("AGENTE")
                .requestMatchers("/api/reasignaciones/casos/*/solicitar").hasRole("AGENTE")

                // Supervisor (CU-06, CU-12)
                .requestMatchers("/api/reasignaciones/pendientes", "/api/reasignaciones/*/aprobar", "/api/reasignaciones/*/rechazar")
                    .hasRole("SUPERVISOR")

                // Administrador (CU-14, CU-15, CU-16)
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.POST, "/api/catalogos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.PUT, "/api/catalogos/**").hasRole("ADMINISTRADOR")
                .requestMatchers(HttpMethod.DELETE, "/api/catalogos/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/parametros/**").hasRole("ADMINISTRADOR")

                // Auditor (CU-17)
                .requestMatchers("/api/bitacoras/**").hasRole("AUDITOR")

                // Compartidos entre Agente/Administrador/Supervisor (CU-10)
                .requestMatchers("/api/casos/buscar").hasAnyRole("AGENTE", "ADMINISTRADOR", "SUPERVISOR")

                // Reportes (CU-12 Admin/Supervisor, CU-13 Auditor)
                .requestMatchers("/api/reportes/casos").hasAnyRole("ADMINISTRADOR", "SUPERVISOR")
                .requestMatchers("/api/reportes/auditoria").hasRole("AUDITOR")

                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.stream(appProperties.getCors().getAllowedOrigins().split(","))
                .map(String::trim).toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
