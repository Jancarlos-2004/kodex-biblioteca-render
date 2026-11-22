package com.kodex.controller;

import com.kodex.model.Ejemplar;
import com.kodex.model.EstadoEjemplar;
import com.kodex.model.Libro;
import com.kodex.repository.EjemplarRepository;
import com.kodex.repository.LibroRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador de Gestión de Ejemplares Físicos (SDD-03, SDD-04).
 * Accesible para ADMIN y CATALOGADOR.
 */
@Controller
@RequestMapping("/ejemplares")
public class EjemplarController {

    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;

    public EjemplarController(EjemplarRepository ejemplarRepository, LibroRepository libroRepository) {
        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar,
                         @RequestParam(value = "estado", required = false) EstadoEjemplar estado,
                         Model model) {
        List<Ejemplar> ejemplares = ejemplarRepository.filtrarEjemplares(buscar, estado);
        model.addAttribute("ejemplares", ejemplares);
        model.addAttribute("estados", EstadoEjemplar.values());
        model.addAttribute("buscar", buscar);
        model.addAttribute("estadoSeleccionado", estado);
        return "ejemplares/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(@RequestParam(value = "idLibro", required = false) Long idLibro, Model model) {
        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setEstado(EstadoEjemplar.DISPONIBLE);
        if (idLibro != null) {
            libroRepository.findById(idLibro).ifPresent(ejemplar::setLibro);
        }
        model.addAttribute("ejemplar", ejemplar);
        model.addAttribute("libros", libroRepository.findAll());
        model.addAttribute("estados", EstadoEjemplar.values());
        return "ejemplares/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("ejemplar") Ejemplar ejemplar,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        // Validar unicidad del código de inventario (SDD-03)
        if (ejemplar.getIdEjemplar() == null) {
            if (ejemplarRepository.existsByCodigoInventario(ejemplar.getCodigoInventario())) {
                bindingResult.rejectValue("codigoInventario", "error.ejemplar", "El código de inventario ya existe en la biblioteca");
            }
        } else {
            if (ejemplarRepository.existsByCodigoInventarioAndIdEjemplarNot(ejemplar.getCodigoInventario(), ejemplar.getIdEjemplar())) {
                bindingResult.rejectValue("codigoInventario", "error.ejemplar", "El código de inventario pertenece a otro ejemplar");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("libros", libroRepository.findAll());
            model.addAttribute("estados", EstadoEjemplar.values());
            return "ejemplares/form";
        }

        ejemplarRepository.save(ejemplar);
        redirectAttributes.addFlashAttribute("mensaje", "Ejemplar físico guardado correctamente con estado " + ejemplar.getEstado().getNombreVisible() + ".");
        return "redirect:/ejemplares";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Ejemplar ejemplar = ejemplarRepository.findById(id).orElse(null);
        if (ejemplar == null) {
            redirectAttributes.addFlashAttribute("error", "Ejemplar no encontrado.");
            return "redirect:/ejemplares";
        }
        model.addAttribute("ejemplar", ejemplar);
        model.addAttribute("libros", libroRepository.findAll());
        model.addAttribute("estados", EstadoEjemplar.values());
        return "ejemplares/form";
    }

    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstado(@PathVariable("id") Long id,
                                @RequestParam("nuevoEstado") EstadoEjemplar nuevoEstado,
                                RedirectAttributes redirectAttributes) {
        Ejemplar ejemplar = ejemplarRepository.findById(id).orElse(null);
        if (ejemplar != null) {
            if (EstadoEjemplar.PRESTADO.equals(ejemplar.getEstado()) && !EstadoEjemplar.PRESTADO.equals(nuevoEstado)) {
                redirectAttributes.addFlashAttribute("error", "No se puede cambiar el estado de un ejemplar actualmente PRESTADO mediante este módulo. Debe registrarse la devolución.");
            } else {
                ejemplar.setEstado(nuevoEstado);
                ejemplarRepository.save(ejemplar);
                redirectAttributes.addFlashAttribute("mensaje", "Estado del ejemplar actualizado a " + nuevoEstado.getNombreVisible() + ".");
            }
        }
        return "redirect:/ejemplares";
    }
}
