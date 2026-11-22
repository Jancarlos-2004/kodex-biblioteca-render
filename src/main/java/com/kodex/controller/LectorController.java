package com.kodex.controller;

import com.kodex.model.EstadoLector;
import com.kodex.model.Lector;
import com.kodex.repository.LectorRepository;
import com.kodex.repository.MultaRepository;
import com.kodex.repository.PrestamoRepository;
import com.kodex.repository.ReservaLibroRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador de Gestión de Lectores (SDD-05, SDD-09).
 * Accesible para ADMIN y BIBLIOTECARIO.
 */
@Controller
@RequestMapping("/lectores")
public class LectorController {

    private final LectorRepository lectorRepository;
    private final PrestamoRepository prestamoRepository;
    private final MultaRepository multaRepository;
    private final ReservaLibroRepository reservaLibroRepository;

    public LectorController(LectorRepository lectorRepository,
                            PrestamoRepository prestamoRepository,
                            MultaRepository multaRepository,
                            ReservaLibroRepository reservaLibroRepository) {
        this.lectorRepository = lectorRepository;
        this.prestamoRepository = prestamoRepository;
        this.multaRepository = multaRepository;
        this.reservaLibroRepository = reservaLibroRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        List<Lector> lectores;
        if (buscar != null && !buscar.isBlank()) {
            lectores = lectorRepository.findByNombreContainingIgnoreCaseOrDocumentoContaining(buscar, buscar);
            model.addAttribute("buscar", buscar);
        } else {
            lectores = lectorRepository.findAll();
        }
        model.addAttribute("lectores", lectores);
        return "lectores/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Lector lector = new Lector();
        lector.setEstado(EstadoLector.ACTIVO);
        model.addAttribute("lector", lector);
        model.addAttribute("estados", EstadoLector.values());
        return "lectores/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("lector") Lector lector,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        // Validar unicidad del documento (SDD-05)
        if (lector.getIdLector() == null) {
            if (lectorRepository.existsByDocumento(lector.getDocumento())) {
                bindingResult.rejectValue("documento", "error.lector", "El documento ya se encuentra registrado para otro lector");
            }
        } else {
            if (lectorRepository.existsByDocumentoAndIdLectorNot(lector.getDocumento(), lector.getIdLector())) {
                bindingResult.rejectValue("documento", "error.lector", "El documento ya pertenece a otro lector registrado");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("estados", EstadoLector.values());
            return "lectores/form";
        }

        lectorRepository.save(lector);
        redirectAttributes.addFlashAttribute("mensaje", "Lector registrado/actualizado exitosamente.");
        return "redirect:/lectores";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Lector lector = lectorRepository.findById(id).orElse(null);
        if (lector == null) {
            redirectAttributes.addFlashAttribute("error", "Lector no encontrado.");
            return "redirect:/lectores";
        }
        model.addAttribute("lector", lector);
        model.addAttribute("estados", EstadoLector.values());
        return "lectores/form";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Lector lector = lectorRepository.findById(id).orElse(null);
        if (lector == null) {
            redirectAttributes.addFlashAttribute("error", "Lector no encontrado.");
            return "redirect:/lectores";
        }
        model.addAttribute("lector", lector);
        model.addAttribute("prestamos", prestamoRepository.findByLectorIdLectorOrderByFechaPrestamoDesc(id));
        model.addAttribute("multas", multaRepository.findByPrestamoLectorIdLectorOrderByPrestamoFechaPrestamoDesc(id));
        model.addAttribute("reservas", reservaLibroRepository.findByLectorIdLectorOrderByFechaSolicitudDesc(id));
        return "lectores/detail";
    }

    @PostMapping("/toggle-sancion/{id}")
    public String toggleSancion(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        lectorRepository.findById(id).ifPresent(l -> {
            if (EstadoLector.ACTIVO.equals(l.getEstado())) {
                l.setEstado(EstadoLector.SANCIONADO);
                redirectAttributes.addFlashAttribute("mensaje", "Lector marcado como SANCIONADO.");
            } else {
                l.setEstado(EstadoLector.ACTIVO);
                redirectAttributes.addFlashAttribute("mensaje", "Sanción levantada. Lector ahora está ACTIVO.");
            }
            lectorRepository.save(l);
        });
        return "redirect:/lectores";
    }
}
