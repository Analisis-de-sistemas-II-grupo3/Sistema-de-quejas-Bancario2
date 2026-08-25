package com.banco.quejas;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra el interceptor de CU01 sobre las bandejas protegidas. La ruta "/" se deja
 * libre para CU00 y "/login" y "/logout" deben permanecer siempre accesibles.
 */
@Configuration
public class ConfiguracionWeb implements WebMvcConfigurer {
    private final AutenticacionInterceptor autenticacionInterceptor;

    public ConfiguracionWeb(AutenticacionInterceptor autenticacionInterceptor) {
        this.autenticacionInterceptor = autenticacionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(autenticacionInterceptor)
                .addPathPatterns("/casos", "/casos/**", "/bandeja/**", "/admin/**", "/auditoria/**");
    }
}
