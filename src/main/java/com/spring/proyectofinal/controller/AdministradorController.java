package com.spring.proyectofinal.controller;

import com.spring.proyectofinal.model.Administrador;
import com.spring.proyectofinal.service.AdministradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/administradores")
public class AdministradorController {

    @Autowired
    private AdministradorService administradorService;

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
            return "redirect:/administradores";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/administradores/nuevo";
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
        return "redirect:/administradores";
    }

    // Ver detalles de un administrador
    @GetMapping("/detalles/{id}")
    public String verDetallesAdministrador(@PathVariable Long id, Model model) {
        Administrador administrador = administradorService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        model.addAttribute("administrador", administrador);
        return "detalles-administrador";
    }
}