package com.spring.proyectofinal;

import com.spring.proyectofinal.model.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ServicioTests {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private ApplicationContext applicationContext;

    @Mock private AdministradorRepository administradorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private SismoRepository sismoRepository;

    @InjectMocks private AdministradorService administradorService;
    @InjectMocks private UsuarioService usuarioService;
    @InjectMocks private SismoServiceImpl sismoService;
    @InjectMocks private DataWarehouseService dataWarehouseService;
    @InjectMocks private DataLoader dataLoader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupJdbcTemplateMocks();
    }

    private void setupJdbcTemplateMocks() {
        // Usamos lenient() para evitar excepciones por stubbings no usados
        lenient().when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class))).thenReturn(0);

        List<Map<String, Object>> mockList = new ArrayList<>();
        Map<String, Object> mockRow = new HashMap<>();
        mockRow.put("estado", "Test Estado");
        mockRow.put("riesgo_promedio", 0.85);
        mockRow.put("cantidad_sismos", 10L);
        mockRow.put("poblacion_afectada_total", 1000000L);
        mockRow.put("impacto_economico_total", 50000.0);
        mockList.add(mockRow);
        lenient().when(jdbcTemplate.queryForList(anyString())).thenReturn(mockList);

        Map<String, Object> mockMap = new HashMap<>();
        mockMap.put("total_sismos", 100L);
        mockMap.put("magnitud_promedio", 4.5);
        mockMap.put("magnitud_maxima", 7.2);
        mockMap.put("poblacion_total", 127000000L);
        lenient().when(jdbcTemplate.queryForMap(anyString())).thenReturn(mockMap);
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

    // Test DataWarehouseService generarAnalisisRiesgo
    @Test
    void testGenerarAnalisisRiesgoSimulado() {
        Map<String, Object> resultado = dataWarehouseService.getMapaRiesgoSismico();

        assertTrue(resultado.containsKey("zonasAltoRiesgo"));
        assertTrue(resultado.containsKey("estadisticasPoblacion"));
        assertTrue(resultado.containsKey("estadosSuperavitarios"));

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
    }

    // Test DataLoader carga archivos SQL simulada
    @Test
    void testCargaArchivosSQL() throws Exception {
        String[] archivos = {
            "data/dim_zonas.sql",
            "data/dim_economia.sql",
            "data/dim_tiempo.sql",
            "data/sismos.sql",
            "data/fact_impacto_sismos.sql"
        };

        for (String archivo : archivos) {
            String contenido = "INSERT INTO tabla VALUES (1);";
            InputStream inputStream = new ByteArrayInputStream(contenido.getBytes(StandardCharsets.UTF_8));
            Resource resource = mock(Resource.class);
            lenient().when(resource.getInputStream()).thenReturn(inputStream);
            lenient().when(applicationContext.getResource("classpath:" + archivo)).thenReturn(resource);
        }

        doNothing().when(jdbcTemplate).execute(anyString());

        assertDoesNotThrow(() -> dataLoader.run());

        verify(jdbcTemplate, atLeastOnce()).execute(anyString());
    }

    // Tests adicionales para DataWarehouseService
    @Test
    void testGetAnalisisEconomico() {
        Map<String, Object> resultado = dataWarehouseService.getAnalisisEconomico();

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("rankingProduccion"));
        assertTrue(resultado.containsKey("estadosSuperavitarios"));
        assertTrue(resultado.containsKey("totalesNacionales"));
    }

    @Test
    void testGetTendenciaTemporal() {
        Map<String, Object> resultado = dataWarehouseService.getTendenciaTemporal();

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("años") || resultado.containsKey("tendenciaCompleta"));
    }

    @Test
    void testGetAnalisisPoblacional() {
        Map<String, Object> resultado = dataWarehouseService.getAnalisisPoblacional();

        assertNotNull(resultado);
        assertTrue(resultado.containsKey("totalPoblacion") || resultado.containsKey("estadisticasPorEstado"));
    }

    @Test
    void testIsDatabasePopulated() {
        // Simulamos que después de cargar, hay datos
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class)))
            .thenReturn(1); // Ahora sí hay registros

        boolean resultado = dataWarehouseService.isDatabasePopulated();
        assertTrue(resultado, "La base debería estar poblada después de la carga");
    }

    @Test
    void testGetSystemStats() {
        Map<String, Object> stats = dataWarehouseService.getSystemStats();

        assertNotNull(stats);
        assertTrue(stats.containsKey("databasePopulated"));
    }

    @Test
    void testGetUltimosSismos() {
        List<Map<String, Object>> mockList = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("estado", "Oaxaca");
        row.put("magnitud", 6.2);
        row.put("fecha", "2024-03-15");
        mockList.add(row);

        String sql = """
            SELECT 
                fecha,
                magnitud,
                latitud,
                longitud,
                profundidad,
                referencia,
                estado,
                hora_utc
            FROM sismos
            ORDER BY fecha DESC
            LIMIT ?
            """;

        when(jdbcTemplate.queryForList(eq(sql), eq(5))).thenReturn(mockList);

        List<Map<String, Object>> resultado = dataWarehouseService.getUltimosSismos(5);

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        assertEquals("Oaxaca", resultado.get(0).get("estado"));
    }
}