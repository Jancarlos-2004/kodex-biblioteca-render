package com.kodex.controller;

import com.kodex.model.*;
import com.kodex.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador del Portal de Lectores (SDD-24).
 * Permite a los lectores consultar exclusivamente sus propios préstamos, reservas y multas.
 */
@Controller
@RequestMapping("/portal")
public class PortalLectorController {

    private final UsuarioRepository usuarioRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final ReservaLibroRepository reservaLibroRepository;
    private final LibroRepository libroRepository;
    private final EjemplarRepository ejemplarRepository;
    private final ConfiguracionRepository configuracionRepository;

    public PortalLectorController(UsuarioRepository usuarioRepository,
                                  PrestamoRepository prestamoRepository,
                                  MultaRepository multaRepository,
                                  ReservaLibroRepository reservaLibroRepository,
                                  LibroRepository libroRepository,
                                  EjemplarRepository ejemplarRepository,
                                  ConfiguracionRepository configuracionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.reservaLibroRepository = reservaLibroRepository;
        this.libroRepository = libroRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.configuracionRepository = configuracionRepository;
    }

    private Lector obtenerLectorAutenticado(Authentication authentication) {
        if (authentication == null) return null;
        Usuario u = usuarioRepository.findByCorreo(authentication.getName()).orElse(null);
        return u != null ? u.getLector() : null;
    }

    @GetMapping
    public String index(Model model, Authentication authentication) {
        Lector lector = obtenerLectorAutenticado(authentication);
        if (lector == null) {
            model.addAttribute("error", "No tiene un perfil de lector asociado a su cuenta de usuario.");
            return "portal/perfil";
        }

        List<Prestamo> prestamosActivos = prestamoRepository.findByLectorIdLectorAndEstado(lector.getIdLector(), EstadoPrestamo.ACTIVO);
        List<Multa> multasPendientes = multaRepository.findByPrestamoLectorIdLectorAndEstado(lector.getIdLector(), EstadoMulta.PENDIENTE);
        List<ReservaLibro> reservas = reservaLibroRepository.findByLectorIdLectorOrderByFechaSolicitudDesc(lector.getIdLector());

        model.addAttribute("lector", lector);
        model.addAttribute("prestamosActivos", prestamosActivos);
        model.addAttribute("multasPendientes", multasPendientes);
        model.addAttribute("reservas", reservas);
        return "portal/perfil";
    }

    @GetMapping("/prestamos")
    public String misPrestamos(Model model, Authentication authentication) {
        Lector lector = obtenerLectorAutenticado(authentication);
        if (lector == null) return "redirect:/portal";

        List<Prestamo> prestamos = prestamoRepository.findByLectorIdLectorOrderByFechaPrestamoDesc(lector.getIdLector());
        model.addAttribute("lector", lector);
        model.addAttribute("prestamos", prestamos);
        return "portal/prestamos";
    }

    @GetMapping("/multas")
    public String misMultas(Model model, Authentication authentication) {
        Lector lector = obtenerLectorAutenticado(authentication);
        if (lector == null) return "redirect:/portal";

        List<Multa> multas = multaRepository.findByPrestamoLectorIdLectorOrderByPrestamoFechaPrestamoDesc(lector.getIdLector());
        model.addAttribute("lector", lector);
        model.addAttribute("multas", multas);
        return "portal/multas";
    }

    @GetMapping("/reservas")
    public String misReservas(Model model, Authentication authentication) {
        Lector lector = obtenerLectorAutenticado(authentication);
        if (lector == null) return "redirect:/portal";

        List<ReservaLibro> reservas = reservaLibroRepository.findByLectorIdLectorOrderByFechaSolicitudDesc(lector.getIdLector());
        model.addAttribute("lector", lector);
        model.addAttribute("reservas", reservas);
        return "portal/reservas";
    }

    @PostMapping("/reservar/{idLibro}")
    public String solicitarReserva(@PathVariable("idLibro") Long idLibro,
                                   Authentication authentication,
                                   RedirectAttributes redirectAttributes) {
        Lector lector = obtenerLectorAutenticado(authentication);
        if (lector == null) {
            redirectAttributes.addFlashAttribute("error", "No se encontró el perfil de lector.");
            return "redirect:/catalogo";
        }

        Libro libro = libroRepository.findById(idLibro).orElse(null);
        if (libro == null) {
            redirectAttributes.addFlashAttribute("error", "Libro no encontrado.");
            return "redirect:/catalogo";
        }

        // Regla SDD-16: Solo cuando no haya ejemplares disponibles
        long disponibles = ejemplarRepository.countByLibroIdLibroAndEstado(libro.getIdLibro(), EstadoEjemplar.DISPONIBLE);
        if (disponibles > 0) {
            redirectAttributes.addFlashAttribute("error", "El libro tiene ejemplares DISPONIBLES en sala. Puede acercarse al bibliotecario para un préstamo directo.");
            return "redirect:/catalogo";
        }

        // Regla: Multas pendientes
        long multasPendientes = multaRepository.countByPrestamoLectorIdLectorAndEstado(lector.getIdLector(), EstadoMulta.PENDIENTE);
        if (multasPendientes > 0) {
            redirectAttributes.addFlashAttribute("error", "No puede realizar reservas porque registra multas pendientes de pago.");
            return "redirect:/catalogo";
        }

        // Regla: No duplicar reserva activa
        if (reservaLibroRepository.existsByLectorIdLectorAndLibroIdLibroAndEstado(lector.getIdLector(), libro.getIdLibro(), EstadoReserva.ACTIVA)) {
            redirectAttributes.addFlashAttribute("error", "Ya tiene una reserva ACTIVA para este libro.");
            return "redirect:/catalogo";
        }

        Configuracion config = configuracionRepository.findAll().stream().findFirst().orElse(new Configuracion());
        int dias = config.getDiasVencimientoReserva() != null ? config.getDiasVencimientoReserva() : 5;

        ReservaLibro reserva = new ReservaLibro(LocalDate.now(), LocalDate.now().plusDays(dias), lector, libro);
        reservaLibroRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensaje", "Su reserva para '" + libro.getTitulo() + "' ha sido registrada exitosamente.");
        return "redirect:/portal/reservas";
    }
}
