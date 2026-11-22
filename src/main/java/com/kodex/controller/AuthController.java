package com.kodex.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de Autenticación y Manejo de Errores de Acceso (SDD-01, SDD-20).
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Authentication authentication,
                        Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas o usuario desactivado. Verifique su correo y contraseña.");
        }
        if (logout != null) {
            model.addAttribute("mensaje", "Ha cerrado su sesión de forma segura.");
        }
        return "login";
    }

    @GetMapping("/error/403")
    public String accesoDenegado(Model model, Authentication authentication) {
        model.addAttribute("titulo", "Acceso Denegado (403)");
        model.addAttribute("mensaje", "No posee los privilegios requeridos para acceder a este módulo del sistema.");
        if (authentication != null) {
            model.addAttribute("usuario", authentication.getName());
            model.addAttribute("roles", authentication.getAuthorities().toString());
        }
        return "error/403";
    }
}
