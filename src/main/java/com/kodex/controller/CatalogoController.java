package com.kodex.controller;

import com.kodex.model.Libro;
import com.kodex.repository.CategoriaRepository;
import com.kodex.repository.EjemplarRepository;
import com.kodex.repository.LibroRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Controlador de Consulta del Catálogo Bibliográfico (SDD-15, SDD-23).
 * Accesible para todos los 6 roles autenticados.
 */
@Controller
@RequestMapping("/catalogo")
public class CatalogoController {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;
    private final EjemplarRepository ejemplarRepository;

    public CatalogoController(LibroRepository libroRepository,
                              CategoriaRepository categoriaRepository,
                              EjemplarRepository ejemplarRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
        this.ejemplarRepository = ejemplarRepository;
    }

    @GetMapping
    public String consultar(@RequestParam(value = "buscar", required = false) String buscar,
                            @RequestParam(value = "categoria", required = false) Long idCategoria,
                            Model model) {
        List<Libro> libros = libroRepository.filtrarCatalogo(buscar, idCategoria);
        model.addAttribute("libros", libros);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNombreAsc());
        model.addAttribute("buscar", buscar);
        model.addAttribute("idCategoriaSeleccionada", idCategoria);
        return "catalogo/index";
    }

    @GetMapping("/detalle/{id}")
    public String detalle(@PathVariable("id") Long id, Model model) {
        Libro libro = libroRepository.findById(id).orElse(null);
        if (libro == null) {
            return "redirect:/catalogo";
        }
        model.addAttribute("libro", libro);
        model.addAttribute("ejemplares", ejemplarRepository.findByLibroIdLibro(id));
        return "catalogo/detail";
    }
}
