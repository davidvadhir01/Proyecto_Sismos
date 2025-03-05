package com.spring.proyectofinal.service;

import com.spring.proyectofinal.model.Administrador;
import com.spring.proyectofinal.repository.AdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AdministradorService {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Crear un nuevo administrador
    @Transactional
    public Administrador crearAdministrador(Administrador administrador) {
        // Verificar si el email ya existe
        if (administradorRepository.existsByEmail(administrador.getEmail())) {
            throw new RuntimeException("El email de administrador ya está registrado");
        }

        // Encriptar la contraseña
        administrador.setPassword(passwordEncoder.encode(administrador.getPassword()));

        // Asegurar que el rol sea ADMIN
        administrador.setRol("ADMIN");

        // Establecer estado activo por defecto
        if (administrador.getEstadoActivo() == null) {
            administrador.setEstadoActivo(true);
        }

        return administradorRepository.save(administrador);
    }

    // Actualizar un administrador existente
    @Transactional
    public Administrador actualizarAdministrador(Long id, Administrador administradorActualizado) {
        Administrador administradorExistente = administradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));

        // Actualizar campos
        administradorExistente.setNombre(administradorActualizado.getNombre());

        // Actualizar email con verificación de duplicados
        if (!administradorExistente.getEmail().equals(administradorActualizado.getEmail())) {
            if (administradorRepository.existsByEmail(administradorActualizado.getEmail())) {
                throw new RuntimeException("El email de administrador ya está registrado");
            }
            administradorExistente.setEmail(administradorActualizado.getEmail());
        }

        // Actualizar departamento
        if (administradorActualizado.getDepartamento() != null) {
            administradorExistente.setDepartamento(administradorActualizado.getDepartamento());
        }

        // Actualizar estado activo
        if (administradorActualizado.getEstadoActivo() != null) {
            administradorExistente.setEstadoActivo(administradorActualizado.getEstadoActivo());
        }

        // Actualizar contraseña solo si se proporciona una nueva
        if (administradorActualizado.getPassword() != null && !administradorActualizado.getPassword().isEmpty()) {
            administradorExistente.setPassword(passwordEncoder.encode(administradorActualizado.getPassword()));
        }

        // Siempre mantener el rol como ADMIN
        administradorExistente.setRol("ADMIN");

        return administradorRepository.save(administradorExistente);
    }

    // Eliminar un administrador
    @Transactional
    public void eliminarAdministrador(Long id) {
        Administrador administrador = administradorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Administrador no encontrado"));
        administradorRepository.delete(administrador);
    }

    // Buscar administrador por ID
    public Optional<Administrador> buscarPorId(Long id) {
        return administradorRepository.findById(id);
    }

    // Buscar administrador por email
    public Optional<Administrador> buscarPorEmail(String email) {
        return administradorRepository.findByEmail(email);
    }

    // Listar todos los administradores
    public List<Administrador> listarTodosLosAdministradores() {
        return administradorRepository.findAll();
    }

    // Listar administradores por departamento
    public List<Administrador> listarAdministradoresPorDepartamento(String departamento) {
        return administradorRepository.findByDepartamento(departamento);
    }

    // Listar administradores por estado (activos/inactivos)
    public List<Administrador> listarAdministradoresPorEstado(Boolean estadoActivo) {
        return administradorRepository.findByEstadoActivo(estadoActivo);
    }
}