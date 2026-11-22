package com.kodex.controller;

import com.kodex.model.Libro;
import com.kodex.repository.CategoriaRepository;
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
 * Controlador de Gestión de Libros del Catálogo (SDD-02, SDD-15).
 * Accesible para ADMIN y CATALOGADOR.
 */
@Controller
@RequestMapping("/libros")
public class LibroController {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final EjemplarRepository ejemplarRepository;

    public LibroController(LibroRepository libroRepository,
                           CategoriaRepository categoriaRepository,
                           EjemplarRepository ejemplarRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.ejemplarRepository = ejemplarRepository;
    }

    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar,
                         @RequestParam(value = "categoria", required = false) Long categoriaId,
                         Model model) {
        List<Libro> libros = libroRepository.filtrarCatalogo(buscar, categoriaId);
        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
        model.addAttribute("buscar", buscar);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        return "libros/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("libro", new Libro());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
        return "libros/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("libro") Libro libro,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        // Validar unicidad de ISBN (SDD-02)
        if (libro.getIdLibro() == null) {
            if (libroRepository.existsByIsbn(libro.getIsbn())) {
                bindingResult.rejectValue("isbn", "error.libro", "El ISBN ya está registrado en el catálogo");
            }
        } else {
            if (libroRepository.existsByIsbnAndIdLibroNot(libro.getIsbn(), libro.getIdLibro())) {
                bindingResult.rejectValue("isbn", "error.libro", "El ISBN ya pertenece a otro libro existente");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
            return "libros/form";
        }

        libroRepository.save(libro);
        redirectAttributes.addFlashAttribute("mensaje", "Libro registrado exitosamente en el catálogo.");
        return "redirect:/libros";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) {
            redirectAttributes.addFlashAttribute("error", "Libro no encontrado.");
            return "redirect:/libros";
        }
        model.addAttribute("libro", libro);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
        return "libros/form";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) {
            redirectAttributes.addFlashAttribute("error", "Libro no encontrado.");
            return "redirect:/libros";
        }
        model.addAttribute("libro", libro);
        model.addAttribute("ejemplares", ejemplarRepository.findByLibroIdLibro(id));
        return "libros/detail";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro != null) {
            if (libro.getEjemplares() != null && !libro.getEjemplares().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar el libro porque tiene ejemplares físicos registrados. Elimine o transfiera primero los ejemplares.");
            } else {
                libroRepository.delete(libro);
                redirectAttributes.addFlashAttribute("mensaje", "Libro eliminado del catálogo exitosamente.");
            }
        }
        return "redirect:/libros";
    }
}
