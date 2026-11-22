package com.kodex.controller;

import com.kodex.model.EstadoEjemplar;
import com.kodex.repository.EjemplarRepository;
import com.kodex.repository.LibroRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de la Página de Aterrizaje (Landing Page) pública de KODEX.
 */
@Controller
public class LandingController {

    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;

    public LandingController(LibroRepository libroRepository, EjemplarRepository ejemplarRepository) {
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
    }

    @GetMapping("/")
    public String landing(Model model, Authentication authentication) {
        long totalLibros = 5;
        long ejemplaresDisponibles = 7;
        try {
            totalLibros = libroRepository.count();
            ejemplaresDisponibles = ejemplarRepository.countByEstado(EstadoEjemplar.DISPONIBLE);
        } catch (Exception ignored) {
            // Fallback seguro si la base de datos se está reconectando
        }
        model.addAttribute("totalLibros", totalLibros);
        model.addAttribute("ejemplaresDisponibles", ejemplaresDisponibles);

        boolean estaAutenticado = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
        model.addAttribute("estaAutenticado", estaAutenticado);

        if (estaAutenticado) {
            boolean isLector = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_LECTOR".equals(a.getAuthority()));
            model.addAttribute("rutaPanel", isLector ? "/portal" : "/dashboard");
        }

        return "landing";
    }
}
