package com.umg.quejasbancario.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.umg.quejasbancario.config.AppProperties;
import com.umg.quejasbancario.exception.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limita las consultas por IP a la consulta publica de casos
 * (GET /api/publico/casos/**, CU-00 FA01) para evitar que se recorran
 * numeros de caso correlativos. Ventana fija por IP, en memoria, configurable
 * en app.rate-limit (max-peticiones por ventana-segundos).
 *
 * Usa request.getRemoteAddr() a proposito: X-Forwarded-For lo controla el
 * cliente y permitiria saltarse el limite. Detras de un proxy de confianza
 * activar server.forward-headers-strategy=framework (FORWARD_HEADERS_STRATEGY).
 */
@Component
@RequiredArgsConstructor
public class PublicRateLimitFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "/api/publico/casos/";

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Ventana> ventanas = new ConcurrentHashMap<>();

    private static final class Ventana {
        long inicioMs;
        int conteo;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !"GET".equalsIgnoreCase(request.getMethod())
                || !request.getRequestURI().startsWith(PREFIJO);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        AppProperties.RateLimit cfg = appProperties.getRateLimit();
        long ventanaMs = Math.max(1, cfg.getVentanaSegundos()) * 1000L;
        long ahora = System.currentTimeMillis();
        String ip = request.getRemoteAddr();

        long reintentarEnSeg = 0;
        boolean permitido;
        Ventana v = ventanas.computeIfAbsent(ip, k -> new Ventana());
        synchronized (v) {
            if (ahora - v.inicioMs >= ventanaMs) {
                v.inicioMs = ahora;
                v.conteo = 0;
            }
            v.conteo++;
            permitido = v.conteo <= cfg.getMaxPeticiones();
            if (!permitido) {
                reintentarEnSeg = Math.max(1, (v.inicioMs + ventanaMs - ahora + 999) / 1000);
            }
        }

        if (permitido) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", String.valueOf(reintentarEnSeg));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ErrorResponse.builder()
                .codigo(HttpStatus.TOO_MANY_REQUESTS.value())
                .mensaje("Demasiadas consultas. Intente nuevamente en unos minutos.")
                .fecha(LocalDateTime.now())
                .build());
    }

    /** Elimina las ventanas vencidas para que el mapa no crezca indefinidamente. */
    @Scheduled(fixedDelay = 60_000)
    public void limpiarVentanasVencidas() {
        long ventanaMs = Math.max(1, appProperties.getRateLimit().getVentanaSegundos()) * 1000L;
        long ahora = System.currentTimeMillis();
        ventanas.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                return ahora - e.getValue().inicioMs >= ventanaMs;
            }
        });
    }
}
