package com.kodex.controller;

import com.kodex.model.Categoria;
import com.kodex.repository.CategoriaRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de Gestión de Categorías de Libros (SDD-02).
 * Accesible para ADMIN y CATALOGADOR.
 */
@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
        return "categorias/list";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categorias/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("categoria") Categoria categoria,
                          BindingResult bindingResult,
                          RedirectAttributes redirectAttributes) {

        // Validar unicidad del nombre
        if (categoria.getIdCategoria() == null) {
            if (categoriaRepository.existsByNombreIgnoreCase(categoria.getNombre())) {
                bindingResult.rejectValue("nombre", "error.categoria", "Ya existe una categoría con este nombre");
            }
        } else {
            if (categoriaRepository.existsByNombreIgnoreCaseAndIdCategoriaNot(categoria.getNombre(), categoria.getIdCategoria())) {
                bindingResult.rejectValue("nombre", "error.categoria", "Ya existe otra categoría con este nombre");
            }
        }

        if (bindingResult.hasErrors()) {
            return "categorias/form";
        }

        categoriaRepository.save(categoria);
        redirectAttributes.addFlashAttribute("mensaje", "Categoría guardada exitosamente.");
        return "redirect:/categorias";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria == null) {
            redirectAttributes.addFlashAttribute("error", "Categoría no encontrada.");
            return "redirect:/categorias";
        }
        model.addAttribute("categoria", categoria);
        return "categorias/form";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        if (categoria != null) {
            if (categoria.getLibros() != null && !categoria.getLibros().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No se puede eliminar la categoría porque contiene libros asociados.");
            } else {
                categoriaRepository.delete(categoria);
                redirectAttributes.addFlashAttribute("mensaje", "Categoría eliminada exitosamente.");
            }
        }
        return "redirect:/categorias";
    }
}
