package com.kodex.controller;

import com.kodex.model.*;
import com.kodex.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Gestión de Reservas de Libros (SDD-16, SDD-17).
 * Accesible para ADMIN y BIBLIOTECARIO.
 */
@Controller
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaLibroRepository reservaLibroRepository;
    private final LibroRepository libroRepository;
    private final LectorRepository lectorRepository;
    private final EjemplarRepository ejemplarRepository;
    private final MultaRepository multaRepository;
    private final ConfiguracionRepository configuracionRepository;

    public ReservaController(ReservaLibroRepository reservaLibroRepository,
                             LibroRepository libroRepository,
                             LectorRepository lectorRepository,
                             EjemplarRepository ejemplarRepository,
                             MultaRepository multaRepository,
                             ConfiguracionRepository configuracionRepository) {
        this.reservaLibroRepository = reservaLibroRepository;
        this.libroRepository = libroRepository;
        this.lectorRepository = lectorRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.multaRepository = multaRepository;
        this.configuracionRepository = configuracionRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "idLibro", required = false) Long idLibro,
                         @RequestParam(value = "idLector", required = false) Long idLector,
                         @RequestParam(value = "estado", required = false) EstadoReserva estado,
                         Model model) {
        // Actualizar automáticamente reservas expiradas si superaron fecha de vencimiento (SDD-17)
        List<ReservaLibro> vencidas = reservaLibroRepository.findReservasVencidas();
        for (ReservaLibro r : vencidas) {
            r.setEstado(EstadoReserva.EXPIRADA);
            reservaLibroRepository.save(r);
        }

        List<ReservaLibro> reservas = reservaLibroRepository.filtrarReservas(idLibro, idLector, estado);
        model.addAttribute("reservas", reservas);
        model.addAttribute("libros", libroRepository.findAll());
        model.addAttribute("lectores", lectorRepository.findAll());
        model.addAttribute("estados", EstadoReserva.values());
        model.addAttribute("idLibroSeleccionado", idLibro);
        model.addAttribute("idLectorSeleccionado", idLector);
        model.addAttribute("estadoSeleccionado", estado);
        return "reservas/list";
    }

    @GetMapping("/nueva")
    public String nueva(@RequestParam(value = "idLibro", required = false) Long idLibro, Model model) {
        model.addAttribute("libros", libroRepository.findAll());
        model.addAttribute("lectores", lectorRepository.findByEstado(EstadoLector.ACTIVO));
        model.addAttribute("idLibroPreseleccionado", idLibro);
        return "reservas/form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam("idLibro") Long idLibro,
                          @RequestParam("idLector") Long idLector,
                          RedirectAttributes redirectAttributes) {

        Libro libro = libroRepository.findById(idLibro).orElse(null);
        Lector lector = lectorRepository.findById(idLector).orElse(null);

        if (libro == null || lector == null) {
            redirectAttributes.addFlashAttribute("error", "Libro o lector inválido.");
            return "redirect:/reservas/nueva";
        }

        // Regla SDD-16: Solo se puede reservar cuando el libro NO tenga ejemplares DISPONIBLES
        long disponibles = ejemplarRepository.countByLibroIdLibroAndEstado(libro.getIdLibro(), EstadoEjemplar.DISPONIBLE);
        if (disponibles > 0) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-16): El libro cuenta con " + disponibles + " ejemplar(es) DISPONIBLE(S). Puede solicitarse directamente como préstamo en lugar de reserva.");
            return "redirect:/reservas/nueva";
        }

        // Regla: No tener multas pendientes
        long multasPendientes = multaRepository.countByPrestamoLectorIdLectorAndEstado(lector.getIdLector(), EstadoMulta.PENDIENTE);
        if (multasPendientes > 0) {
            redirectAttributes.addFlashAttribute("error", "Rechazado: El lector posee multas pendientes y no puede reservar.");
            return "redirect:/reservas/nueva";
        }

        // Regla: Evitar doble reserva activa del mismo lector sobre el mismo libro
        if (reservaLibroRepository.existsByLectorIdLectorAndLibroIdLibroAndEstado(lector.getIdLector(), libro.getIdLibro(), EstadoReserva.ACTIVA)) {
            redirectAttributes.addFlashAttribute("error", "El lector ya cuenta con una reserva ACTIVA para este libro.");
            return "redirect:/reservas/nueva";
        }

        Configuracion config = configuracionRepository.findAll().stream().findFirst().orElse(new Configuracion());
        int diasVigencia = config.getDiasVencimientoReserva() != null ? config.getDiasVencimientoReserva() : 5;

        LocalDate hoy = LocalDate.now();
        LocalDate vencimiento = hoy.plusDays(diasVigencia);

        ReservaLibro reserva = new ReservaLibro(hoy, vencimiento, lector, libro);
        reservaLibroRepository.save(reserva);

        redirectAttributes.addFlashAttribute("mensaje", "Reserva ACTIVA registrada con éxito. Fecha de vigencia hasta: " + vencimiento + " (SDD-16).");
        return "redirect:/reservas";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        ReservaLibro reserva = reservaLibroRepository.findById(id).orElse(null);
        if (reserva != null && EstadoReserva.ACTIVA.equals(reserva.getEstado())) {
            reserva.setEstado(EstadoReserva.CANCELADA);
            reservaLibroRepository.save(reserva);
            redirectAttributes.addFlashAttribute("mensaje", "Reserva CANCELADA exitosamente (SDD-17).");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se puede cancelar la reserva seleccionada.");
        }
        return "redirect:/reservas";
    }
}
