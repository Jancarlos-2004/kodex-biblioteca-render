package com.kodex.controller;

import com.kodex.model.*;
import com.kodex.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador del Dashboard Central Administrativo (SDD-01).
 * Agrega KPIs, estadísticas e información del estado operativo de la biblioteca.
 */
@Controller
public class DashboardController {

    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final LectorRepository lectorRepository;
    private final ReservaLibroRepository reservaLibroRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardController(LibroRepository libroRepository,
                               EjemplarRepository ejemplarRepository,
                               PrestamoRepository prestamoRepository,
                               MultaRepository multaRepository,
                               LectorRepository lectorRepository,
                               ReservaLibroRepository reservaLibroRepository,
                               UsuarioRepository usuarioRepository) {
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.lectorRepository = lectorRepository;
        this.reservaLibroRepository = reservaLibroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/dashboard")
    public String index(Model model, Authentication authentication) {
        if (authentication != null) {
            boolean isLector = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_LECTOR"));
            if (isLector) {
                return "redirect:/portal";
            }
            usuarioRepository.findByCorreo(authentication.getName()).ifPresent(u -> model.addAttribute("usuarioLogueado", u));
        }

        // Métricas e Indicadores
        long totalLibros = libroRepository.count();
        long totalEjemplares = ejemplarRepository.count();
        long ejemplaresDisponibles = ejemplarRepository.countByEstado(EstadoEjemplar.DISPONIBLE);
        long ejemplaresPrestados = ejemplarRepository.countByEstado(EstadoEjemplar.PRESTADO);
        long ejemplaresReparacion = ejemplarRepository.countByEstado(EstadoEjemplar.EN_REPARACION);

        long totalLectores = lectorRepository.count();
        long lectoresActivos = lectorRepository.findByEstado(EstadoLector.ACTIVO).size();

        long prestamosActivos = prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO);
        long reservasActivas = reservaLibroRepository.countByEstado(EstadoReserva.ACTIVA);

        List<Prestamo> prestamosVencidos = prestamoRepository.findPrestamosVencidos();
        long totalMultasPendientes = multaRepository.countByEstado(EstadoMulta.PENDIENTE);
        BigDecimal recaudoTotal = multaRepository.sumTotalRecaudado();
        BigDecimal multasPendientesMonto = multaRepository.sumTotalPendiente();

        model.addAttribute("totalLibros", totalLibros);
        model.addAttribute("totalEjemplares", totalEjemplares);
        model.addAttribute("ejemplaresDisponibles", ejemplaresDisponibles);
        model.addAttribute("ejemplaresPrestados", ejemplaresPrestados);
        model.addAttribute("ejemplaresReparacion", ejemplaresReparacion);
        model.addAttribute("totalLectores", totalLectores);
        model.addAttribute("lectoresActivos", lectoresActivos);
        model.addAttribute("prestamosActivos", prestamosActivos);
        model.addAttribute("reservasActivas", reservasActivas);
        model.addAttribute("prestamosVencidosCount", prestamosVencidos.size());
        model.addAttribute("totalMultasPendientes", totalMultasPendientes);
        model.addAttribute("recaudoTotal", recaudoTotal);
        model.addAttribute("multasPendientesMonto", multasPendientesMonto);

        // Listas recientes para el dashboard
        model.addAttribute("prestamosRecientes", prestamoRepository.findByEstadoOrderByFechaLimiteAsc(EstadoPrestamo.ACTIVO));
        model.addAttribute("librosPopulares", libroRepository.findLibrosMasPrestados());

        return "dashboard/index";
    }
}
