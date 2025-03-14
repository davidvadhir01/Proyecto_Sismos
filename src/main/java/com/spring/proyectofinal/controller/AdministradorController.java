package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Administrador;
import com.spring.proyectofinal.model.Usuario;
import com.spring.proyectofinal.service.AdministradorService;
import com.spring.proyectofinal.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

    @Autowired
    private UsuarioService usuarioService;

    // Listar todos los administradores
    @GetMapping
    public String listarAdministradores(Model model) {
        model.addAttribute("administradores", administradorService.listarTodosLosAdministradores());
        return "lista-administradores";
    }

    // Mostrar formulario para nuevo administrador
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("administrador", new Administrador());
        return "formulario-administrador";
    }

    // Mostrar formulario para editar administrador
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable Long id, Model model) {
        Administrador administrador = administradorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        model.addAttribute("administrador", administrador);
        return "formulario-administrador";
    }

    // Guardar/Actualizar administrador
    @PostMapping("/guardar")
    public String guardarAdministrador(@ModelAttribute Administrador administrador,
                                       RedirectAttributes redirectAttributes) {
        try {
            if (administrador.getId() == null) {
                // Crear nuevo administrador
                administradorService.crearAdministrador(administrador);
                redirectAttributes.addFlashAttribute("mensaje", "Administrador creado exitosamente");
            } else {
                // Actualizar administrador existente
                administradorService.actualizarAdministrador(administrador.getId(), administrador);
                redirectAttributes.addFlashAttribute("mensaje", "Administrador actualizado exitosamente");
            }
            return "redirect:/admin";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/nuevo";
        }
    }

    // Eliminar administrador
    @GetMapping("/eliminar/{id}")
    public String eliminarAdministrador(@PathVariable Long id,
                                        RedirectAttributes redirectAttributes) {
        try {
            administradorService.eliminarAdministrador(id);
            redirectAttributes.addFlashAttribute("mensaje", "Administrador eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el administrador");
        }
        return "redirect:/admin";
    }

    // Ver detalles de un administrador
    @GetMapping("/detalles/{id}")
    public String verDetallesAdministrador(@PathVariable Long id, Model model) {
        Administrador administrador = administradorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        model.addAttribute("administrador", administrador);
        return "detalles-administrador";
    }

    @GetMapping("/perfil")
    public String perfilAdmin(Model model, Authentication auth) {
        Usuario admin = usuarioService.buscarPorEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("admin", admin);
        return "admin/perfilAdmin";  
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodosLosUsuarios());
        return "admin/gestionUser";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/registro";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editarUsuario(@PathVariable Long id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        return "admin/registro";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            if (usuario.getId() == null) {
                usuarioService.crearUsuario(usuario);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario creado exitosamente");
            } else {
                usuarioService.actualizarUsuario(usuario.getId(), usuario);
                redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado exitosamente");
            }
            return "redirect:/admin/usuarios";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario");
        }
        return "redirect:/admin/usuarios";
    }
}