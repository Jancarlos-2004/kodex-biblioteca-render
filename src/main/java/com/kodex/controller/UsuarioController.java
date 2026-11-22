package com.kodex.controller;

import com.kodex.model.Lector;
import com.kodex.model.Rol;
import com.kodex.model.Usuario;
import com.kodex.repository.LectorRepository;
import com.kodex.repository.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador de Gestión de Usuarios y Roles (Solo Rol ADMIN) (SDD-01, SDD-20).
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final LectorRepository lectorRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository,
                             LectorRepository lectorRepository,
                             PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.lectorRepository = lectorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String listar(@RequestParam(value = "buscar", required = false) String buscar, Model model) {
        List<Usuario> usuarios;
        if (buscar != null && !buscar.isBlank()) {
            usuarios = usuarioRepository.findByNombreContainingIgnoreCaseOrCorreoContainingIgnoreCase(buscar, buscar);
            model.addAttribute("buscar", buscar);
        } else {
            usuarios = usuarioRepository.findAll();
        }
        model.addAttribute("usuarios", usuarios);
        return "usuarios/list";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        Usuario usuario = new Usuario();
        usuario.setActivo(true);
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("lectores", lectorRepository.findAll());
        return "usuarios/form";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("usuario") Usuario usuario,
                          BindingResult bindingResult,
                          @RequestParam(value = "nuevaClave", required = false) String nuevaClave,
                          @RequestParam(value = "idLector", required = false) Long idLector,
                          RedirectAttributes redirectAttributes,
                          Model model) {

        // Validar unicidad de correo
        if (usuario.getIdUsuario() == null) {
            if (usuarioRepository.existsByCorreo(usuario.getCorreo())) {
                bindingResult.rejectValue("correo", "error.usuario", "El correo ya está registrado en el sistema");
            }
            if (nuevaClave == null || nuevaClave.isBlank()) {
                bindingResult.rejectValue("claveHash", "error.usuario", "La contraseña es obligatoria para un usuario nuevo");
            }
        } else {
            if (usuarioRepository.existsByCorreoAndIdUsuarioNot(usuario.getCorreo(), usuario.getIdUsuario())) {
                bindingResult.rejectValue("correo", "error.usuario", "El correo ya pertenece a otro usuario");
            }
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("lectores", lectorRepository.findAll());
            return "usuarios/form";
        }

        if (idLector != null) {
            Lector lector = lectorRepository.findById(idLector).orElse(null);
            usuario.setLector(lector);
        } else {
            usuario.setLector(null);
        }

        if (nuevaClave != null && !nuevaClave.isBlank()) {
            usuario.setClaveHash(passwordEncoder.encode(nuevaClave));
        } else if (usuario.getIdUsuario() != null) {
            // Mantener clave existente si no se ingresó una nueva
            usuarioRepository.findById(usuario.getIdUsuario()).ifPresent(existente -> {
                usuario.setClaveHash(existente.getClaveHash());
            });
        }

        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("mensaje", "Usuario guardado exitosamente.");
        return "redirect:/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
            return "redirect:/usuarios";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("lectores", lectorRepository.findAll());
        return "usuarios/form";
    }

    @PostMapping("/toggle-estado/{id}")
    public String toggleEstado(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setActivo(!Boolean.TRUE.equals(u.getActivo()));
            usuarioRepository.save(u);
            redirectAttributes.addFlashAttribute("mensaje", "Estado del usuario actualizado con éxito.");
        });
        return "redirect:/usuarios";
    }
}
