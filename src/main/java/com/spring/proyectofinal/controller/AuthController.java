package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Usuario;
import com.spring.proyectofinal.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

@Controller
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UsuarioService usuarioService;

    // Método para mostrar formulario de registro
    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        // Si no hay un usuario en el modelo, agrégalo
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
        return "registro";
    }

    // Método para procesar el registro
    @PostMapping("/registro")
    public String registrarUsuario(
            @ModelAttribute("usuario") Usuario usuario,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        // Validaciones básicas
        if (bindingResult.hasErrors()) {
            return "registro";
        }

        // Verificar si el email ya existe
        if (usuarioService.buscarPorEmail(usuario.getEmail()).isPresent()) {
            model.addAttribute("errorEmail", "El correo electrónico ya está registrado");
            return "registro";
        }

        try {
            // Registrar usuario
            usuarioService.registrarUsuario(usuario);

            // Agregar mensaje de éxito
            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Inicia sesión.");

            // Redirigir a login
            return "redirect:/auth/login";
        } catch (Exception e) {
            // Manejar cualquier error de registro
            model.addAttribute("error", "Ocurrió un error al registrar el usuario");
            return "registro";
        }
    }

    // Método para mostrar página de login
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // Método para logout
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/auth/login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/perfil";
        }
        return "redirect:/perfil";
    }

    
}