package com.kodex.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Configuración de Seguridad Spring Security (NFR-02, SDD-01, SDD-20).
 * Protege rutas de acuerdo a los 6 roles de KODEX y cifra contraseñas con BCrypt.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(auth -> auth
                // Recursos estáticos y Landing Page pública
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers("/login", "/error/**").permitAll()

                // Portal de Lectores (SDD-24)
                .requestMatchers("/portal/**").hasRole("LECTOR")

                // Módulo de Usuarios y Configuración (Solo ADMIN)
                .requestMatchers("/usuarios/**", "/configuracion/**").hasRole("ADMIN")

                // Catálogo y Ejemplares (ADMIN y CATALOGADOR)
                .requestMatchers("/categorias/**", "/libros/**", "/ejemplares/**").hasAnyRole("ADMIN", "CATALOGADOR")

                // Lectores y Préstamos (ADMIN y BIBLIOTECARIO)
                .requestMatchers("/lectores/**", "/prestamos/**").hasAnyRole("ADMIN", "BIBLIOTECARIO")

                // Multas y Caja (ADMIN y TESORERO)
                .requestMatchers("/multas/**").hasAnyRole("ADMIN", "TESORERO")

                // Reservas (ADMIN, BIBLIOTECARIO)
                .requestMatchers("/reservas/**").hasAnyRole("ADMIN", "BIBLIOTECARIO")

                // Reportes e Indicadores (ADMIN y GERENTE)
                .requestMatchers("/reportes/**").hasAnyRole("ADMIN", "GERENTE")

                // Consulta de catálogo y Dashboard común
                .requestMatchers("/catalogo/**").hasAnyRole("ADMIN", "BIBLIOTECARIO", "CATALOGADOR", "TESORERO", "GERENTE", "LECTOR")
                .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "BIBLIOTECARIO", "CATALOGADOR", "TESORERO", "GERENTE")

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("correo")
                .passwordParameter("clave")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedHandler(accessDeniedHandler())
            );

        return http.build();
    }

    /**
     * Redirige al dashboard adecuado según el rol autenticado:
     * El Lector va directamente a su Portal personal (/portal); los demás al Dashboard administrativo (/dashboard).
     */
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            boolean isLector = false;
            for (GrantedAuthority auth : authentication.getAuthorities()) {
                if ("ROLE_LECTOR".equals(auth.getAuthority())) {
                    isLector = true;
                    break;
                }
            }
            if (isLector) {
                response.sendRedirect(request.getContextPath() + "/portal");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect(request.getContextPath() + "/error/403");
        };
    }
}
