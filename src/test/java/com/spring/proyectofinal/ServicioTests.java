package com.spring.proyectofinal;

import com.spring.proyectofinal.model.*;
//import com.spring.proyectofinal.model.datawarehouse.*;
import com.spring.proyectofinal.repository.*;
import com.spring.proyectofinal.service.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicioTests {

    // Mocks
    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ApplicationContext applicationContext;

    @Mock private AdministradorRepository administradorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SismoRepository sismoRepository;

    // Servicios inyectados automáticamente con mocks
    @InjectMocks private AdministradorService administradorService;
    @InjectMocks private UsuarioService usuarioService;
    @InjectMocks private SismoServiceImpl sismoService;
    @InjectMocks private DataWarehouseService dataWarehouseService;
    @InjectMocks private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // No necesitas llamar a setters ni instanciar manualmente dataLoader
    }

    // Test AdministradorService
    @Test
    void testGuardarAdministrador() {
        Administrador admin = new Administrador();
        admin.setNombre("Lizeth");
        when(administradorRepository.save(admin)).thenReturn(admin);

        Administrador result = administradorService.crearAdministrador(admin);
        assertEquals("Lizeth", result.getNombre());
        verify(administradorRepository).save(admin);
    }

    // Test UsuarioService registrarUsuario
    @Test
    void testRegistrarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setPassword("1234");
        usuario.setEmail("test@email.com");

        when(passwordEncoder.encode("1234")).thenReturn("hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario registrado = usuarioService.registrarUsuario(usuario);
        assertEquals("hashed", registrado.getPassword());
        assertEquals("USER", registrado.getRol());
    }

    // Test UsuarioService loadUserByUsername
    @Test
    void testLoadUserByUsername() {
        Usuario usuario = new Usuario();
        usuario.setEmail("correo@correo.com");
        usuario.setPassword("clave");
        usuario.setRol("ADMIN");

        when(usuarioRepository.findByEmail("correo@correo.com")).thenReturn(Optional.of(usuario));

        UserDetails details = usuarioService.loadUserByUsername("correo@correo.com");
        assertEquals("correo@correo.com", details.getUsername());
        assertEquals("clave", details.getPassword());
    }

    // Test SismoServiceImpl obtenerSismosPorAño
    @Test
    void testObtenerSismosPorAño() {
        int year = 2022;
        List<Sismo> lista = List.of(new Sismo());
        when(sismoRepository.findByYear(year)).thenReturn(lista);

        List<Sismo> resultado = sismoService.obtenerSismosPorAño(year);
        assertEquals(1, resultado.size());
    }

    // Test SismoServiceImpl obtenerSismosRecientes
    @Test
    void testObtenerSismosRecientes() {
        List<Sismo> recientes = List.of(new Sismo());
        when(sismoRepository.findByFechaAfter(any(LocalDateTime.class))).thenReturn(recientes);

        List<Sismo> resultado = sismoService.obtenerSismosRecientes();
        assertFalse(resultado.isEmpty());
    }

    // Test DataWarehouseService generarAnalisisRiesgo (simulado)
    @Test
    void testGenerarAnalisisRiesgoSimulado() {
       Map<String, Object> resultado = dataWarehouseService.getMapaRiesgoSismico();
        assertTrue(resultado.containsKey("nivelRiesgo"));
    }

    // Test DataLoader carga archivos SQL simulada
    @Test
    void testCargaArchivosSQL() throws Exception {
        String[] archivos = {
            "sql/dim_zonas.sql",
            "sql/dim_economia.sql",
            "sql/dim_tiempo.sql",
            "sql/sismos.sql",
            "sql/fact_impacto_sismos.sql"
        };

        for (String archivo : archivos) {
            String contenido = "INSERT INTO tabla VALUES (1);";
            InputStream inputStream = new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
            Resource resource = mock(Resource.class);
            when(resource.getInputStream()).thenReturn(inputStream);
            when(applicationContext.getResource("classpath:" + archivo)).thenReturn(resource);
        }

        dataLoader.run();

        verify(jdbcTemplate, times(5)).execute("INSERT INTO tabla VALUES (1);");
    }
}