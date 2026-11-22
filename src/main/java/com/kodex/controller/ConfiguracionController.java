package com.kodex.controller;

import com.kodex.model.Configuracion;
import com.kodex.repository.ConfiguracionRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador de Configuración del Sistema (Solo Rol ADMIN).
 * Permite ajustar los parámetros operativos (plazo de préstamo, valor de multa, etc.).
 */
@Controller
@RequestMapping("/configuracion")
public class ConfiguracionController {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionController(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    @GetMapping
    public String verConfiguracion(Model model) {
        Configuracion config = configuracionRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Configuracion c = new Configuracion();
                    return configuracionRepository.save(c);
                });
        model.addAttribute("configuracion", config);
        return "configuracion/index";
    }

    @PostMapping("/guardar")
    public String guardarConfiguracion(@Valid @ModelAttribute("configuracion") Configuracion configuracion,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        if (bindingResult.hasErrors()) {
            return "configuracion/index";
        }

        configuracionRepository.save(configuracion);
        redirectAttributes.addFlashAttribute("mensaje", "Configuración del sistema actualizada correctamente.");
        return "redirect:/configuracion";
    }
}
