package com.kodex.controller;

import com.kodex.model.*;
import com.kodex.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controlador de Gestión de Préstamos, Renovaciones y Devoluciones (SDD-06, SDD-07, SDD-08, SDD-09, SDD-10, SDD-11, SDD-12).
 * Accesible para ADMIN y BIBLIOTECARIO.
 */
@Controller
@RequestMapping("/prestamos")
public class PrestamoController {

    private final PrestamoRepository prestamoRepository;
    private final LectorRepository lectorRepository;
    private final EjemplarRepository ejemplarRepository;
    private final UsuarioRepository usuarioRepository;
    private final MultaRepository multaRepository;
    private final ReservaLibroRepository reservaLibroRepository;
    private final ConfiguracionRepository configuracionRepository;

    public PrestamoController(PrestamoRepository prestamoRepository,
                              LectorRepository lectorRepository,
                              EjemplarRepository ejemplarRepository,
                              UsuarioRepository usuarioRepository,
                              MultaRepository multaRepository,
                              ReservaLibroRepository reservaLibroRepository,
                              ConfiguracionRepository configuracionRepository) {
        this.prestamoRepository = prestamoRepository;
        this.lectorRepository = lectorRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.usuarioRepository = usuarioRepository;
        this.multaRepository = multaRepository;
        this.reservaLibroRepository = reservaLibroRepository;
        this.configuracionRepository = configuracionRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "idLector", required = false) Long idLector,
                         @RequestParam(value = "estado", required = false) EstadoPrestamo estado,
                         @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        List<Prestamo> prestamos = prestamoRepository.filtrarPrestamos(idLector, estado, desde, hasta);
        model.addAttribute("prestamos", prestamos);
        model.addAttribute("lectores", lectorRepository.findAll());
        model.addAttribute("estados", EstadoPrestamo.values());
        model.addAttribute("idLectorSeleccionado", idLector);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        return "prestamos/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("lectores", lectorRepository.findByEstado(EstadoLector.ACTIVO));
        model.addAttribute("ejemplaresDisponibles", ejemplarRepository.findByEstado(EstadoEjemplar.DISPONIBLE));
        return "prestamos/form";
    }

    @PostMapping("/guardar")
    public String guardar(@RequestParam("idLector") Long idLector,
                          @RequestParam("idEjemplar") Long idEjemplar,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        Lector lector = lectorRepository.findById(idLector).orElse(null);
        Ejemplar ejemplar = ejemplarRepository.findById(idEjemplar).orElse(null);
        Usuario usuario = usuarioRepository.findByCorreo(authentication.getName()).orElse(null);

        if (lector == null || ejemplar == null || usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Datos incompletos para registrar el préstamo.");
            return "redirect:/prestamos/nuevo";
        }

        // Regla 1: Validar que el lector esté ACTIVO (SDD-05)
        if (EstadoLector.SANCIONADO.equals(lector.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Rechazado: El lector se encuentra SANCIONADO y no puede solicitar préstamos.");
            return "redirect:/prestamos/nuevo";
        }

        // Regla 2: Validar multas pendientes (SDD-09)
        long multasPendientes = multaRepository.countByPrestamoLectorIdLectorAndEstado(lector.getIdLector(), EstadoMulta.PENDIENTE);
        if (multasPendientes > 0) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-09): El lector tiene " + multasPendientes + " multa(s) PENDIENTE(S). Debe estar a paz y salvo.");
            return "redirect:/prestamos/nuevo";
        }

        // Regla 3: Validar límite máximo de préstamos activos (SDD-08)
        Configuracion config = configuracionRepository.findAll().stream().findFirst().orElse(new Configuracion());
        int maxPrestamos = config.getMaxPrestamosActivos() != null ? config.getMaxPrestamosActivos() : 3;
        long prestamosActivos = prestamoRepository.countByLectorIdLectorAndEstado(lector.getIdLector(), EstadoPrestamo.ACTIVO);
        if (prestamosActivos >= maxPrestamos) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-08): El lector ya tiene el número máximo permitido de préstamos activos (" + maxPrestamos + ").");
            return "redirect:/prestamos/nuevo";
        }

        // Regla 4: Validar disponibilidad del ejemplar (SDD-03, SDD-04, SDD-07)
        if (!EstadoEjemplar.DISPONIBLE.equals(ejemplar.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-04/SDD-07): El ejemplar no está disponible. Estado actual: " + ejemplar.getEstado().getNombreVisible());
            return "redirect:/prestamos/nuevo";
        }

        // Registrar Préstamo (SDD-06)
        int dias = config.getDiasPrestamo() != null ? config.getDiasPrestamo() : 7;
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);

        Prestamo prestamo = new Prestamo(hoy, limite, lector, ejemplar, usuario);
        prestamoRepository.save(prestamo);

        // Cambiar estado del ejemplar a PRESTADO
        ejemplar.setEstado(EstadoEjemplar.PRESTADO);
        ejemplarRepository.save(ejemplar);

        redirectAttributes.addFlashAttribute("mensaje", "Préstamo registrado exitosamente. Fecha límite de devolución: " + limite + ".");
        return "redirect:/prestamos";
    }

    @PostMapping("/renovar/{id}")
    public String renovar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Prestamo prestamo = prestamoRepository.findById(id).orElse(null);
        if (prestamo == null) {
            redirectAttributes.addFlashAttribute("error", "Préstamo no encontrado.");
            return "redirect:/prestamos";
        }

        // Regla: Préstamo debe estar ACTIVO (SDD-10)
        if (!EstadoPrestamo.ACTIVO.equals(prestamo.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Rechazado: Solo se pueden renovar préstamos en estado ACTIVO.");
            return "redirect:/prestamos";
        }

        // Regla: Máximo 1 renovación permitida (SDD-10)
        if (prestamo.getRenovaciones() != null && prestamo.getRenovaciones() >= 1) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-10): Este préstamo ya fue renovado una vez. No se permiten renovaciones adicionales.");
            return "redirect:/prestamos";
        }

        // Regla: No tener multas pendientes (SDD-10)
        long multasPendientes = multaRepository.countByPrestamoLectorIdLectorAndEstado(prestamo.getLector().getIdLector(), EstadoMulta.PENDIENTE);
        if (multasPendientes > 0) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-10): El lector presenta multas pendientes de pago.");
            return "redirect:/prestamos";
        }

        // Regla: No existir reserva activa sobre el libro (SDD-10)
        Long idLibro = prestamo.getEjemplar().getLibro().getIdLibro();
        long reservasActivas = reservaLibroRepository.countByLibroIdLibroAndEstado(idLibro, EstadoReserva.ACTIVA);
        if (reservasActivas > 0) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-10): Existe al menos una reserva activa sobre este libro por parte de otro usuario.");
            return "redirect:/prestamos";
        }

        Configuracion config = configuracionRepository.findAll().stream().findFirst().orElse(new Configuracion());
        int dias = config.getDiasPrestamo() != null ? config.getDiasPrestamo() : 7;

        prestamo.setFechaLimite(prestamo.getFechaLimite().plusDays(dias));
        prestamo.setRenovaciones(prestamo.getRenovaciones() + 1);
        prestamoRepository.save(prestamo);

        redirectAttributes.addFlashAttribute("mensaje", "Préstamo renovado exitosamente. Nueva fecha límite: " + prestamo.getFechaLimite() + ".");
        return "redirect:/prestamos";
    }

    @PostMapping("/devolver/{id}")
    public String devolver(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Prestamo prestamo = prestamoRepository.findById(id).orElse(null);
        if (prestamo == null) {
            redirectAttributes.addFlashAttribute("error", "Préstamo no encontrado.");
            return "redirect:/prestamos";
        }

        if (EstadoPrestamo.DEVUELTO.equals(prestamo.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Este préstamo ya se encuentra devuelto.");
            return "redirect:/prestamos";
        }

        LocalDate hoy = LocalDate.now();
        prestamo.setFechaDevolucion(hoy);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamoRepository.save(prestamo);

        // Liberar el ejemplar (SDD-11)
        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        ejemplarRepository.save(ejemplar);

        // Validar si hubo retraso y calcular multa (SDD-12)
        if (hoy.isAfter(prestamo.getFechaLimite())) {
            long diasRetraso = ChronoUnit.DAYS.between(prestamo.getFechaLimite(), hoy);
            Configuracion config = configuracionRepository.findAll().stream().findFirst().orElse(new Configuracion());
            BigDecimal valorDiario = config.getValorMultaDiaria() != null ? config.getValorMultaDiaria() : new BigDecimal("1000.00");
            BigDecimal totalMulta = valorDiario.multiply(BigDecimal.valueOf(diasRetraso));

            Multa multa = new Multa((int) diasRetraso, totalMulta, prestamo);
            multaRepository.save(multa);

            // Sancionar temporalmente al lector hasta que pague la multa
            Lector lector = prestamo.getLector();
            lector.setEstado(EstadoLector.SANCIONADO);
            lectorRepository.save(lector);

            redirectAttributes.addFlashAttribute("error", "Devolución con " + diasRetraso + " día(s) de retraso. Se generó una multa por $" + totalMulta + " (SDD-12). El lector queda sancionado temporalmente.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Devolución registrada exitosamente a tiempo. El ejemplar queda DISPONIBLE (SDD-11).");
        }

        return "redirect:/prestamos";
    }
}
