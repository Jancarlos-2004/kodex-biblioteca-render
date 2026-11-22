package com.kodex.controller;

import com.kodex.model.EstadoLector;
import com.kodex.model.EstadoMulta;
import com.kodex.model.Lector;
import com.kodex.model.Multa;
import com.kodex.repository.LectorRepository;
import com.kodex.repository.MultaRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador de Gestión de Multas y Caja (SDD-13, SDD-14, SDD-19).
 * Accesible para ADMIN y TESORERO.
 */
@Controller
@RequestMapping("/multas")
public class MultaController {

    private final MultaRepository multaRepository;
    private final LectorRepository lectorRepository;

    public MultaController(MultaRepository multaRepository, LectorRepository lectorRepository) {
        this.multaRepository = multaRepository;
        this.lectorRepository = lectorRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "estado", required = false) EstadoMulta estado,
                         @RequestParam(value = "desde", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                         @RequestParam(value = "hasta", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                         Model model) {
        List<Multa> multas = multaRepository.filtrarMultas(estado, desde, hasta);
        model.addAttribute("multas", multas);
        model.addAttribute("estados", EstadoMulta.values());
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("totalRecaudado", multaRepository.sumTotalRecaudado());
        model.addAttribute("totalPendiente", multaRepository.sumTotalPendiente());
        return "multas/list";
    }

    @PostMapping("/pagar/{id}")
    public String registrarPago(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Multa multa = multaRepository.findById(id).orElse(null);

        // Validación de existencia (SDD-14)
        if (multa == null) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-14): La multa solicitada no existe.");
            return "redirect:/multas";
        }

        // Validación de estado previo: evitar cobros dobles (SDD-14)
        if (EstadoMulta.PAGADA.equals(multa.getEstado())) {
            redirectAttributes.addFlashAttribute("error", "Rechazado (SDD-14): La multa ya fue cancelada previamente el " + multa.getFechaPago() + ".");
            return "redirect:/multas";
        }

        // Registrar pago (SDD-13)
        multa.setEstado(EstadoMulta.PAGADA);
        multa.setFechaPago(LocalDate.now());
        multaRepository.save(multa);

        // Verificar si el lector ya no tiene ninguna otra multa pendiente y restaurar su estado ACTIVO
        Lector lector = multa.getPrestamo().getLector();
        long restantes = multaRepository.countByPrestamoLectorIdLectorAndEstado(lector.getIdLector(), EstadoMulta.PENDIENTE);
        if (restantes == 0 && EstadoLector.SANCIONADO.equals(lector.getEstado())) {
            lector.setEstado(EstadoLector.ACTIVO);
            lectorRepository.save(lector);
            redirectAttributes.addFlashAttribute("mensaje", "Pago de $" + multa.getValor() + " registrado exitosamente. Al no tener multas pendientes, el lector ha sido restablecido a estado ACTIVO.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "Pago de $" + multa.getValor() + " registrado exitosamente. Fecha de pago: " + multa.getFechaPago() + ".");
        }

        return "redirect:/multas";
    }
}
