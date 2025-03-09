package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Usuario;
import com.spring.proyectofinal.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mensaje", "¡Bienvenido! Usa el formulario para registrarte.");
        return "index"; // Renderiza index.html
    }

    @GetMapping("/perfil")
    public String perfil(Model model, Principal principal) {
        String email = principal.getName(); // Obtiene el email del usuario autenticado
        Usuario usuario = usuarioService.buscarPorEmail(email).orElseThrow();
        model.addAttribute("usuario", usuario);
        return "perfil"; // Renderiza perfil.html
    }
}