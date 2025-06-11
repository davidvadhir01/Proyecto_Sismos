-- Script SQL Combinado para Docker
-- Incluye: BD original + Datawarehouse + Datos de ejemplo

CREATE DATABASE IF NOT EXISTS sismos;
USE sismos;

-- =============================================
-- TABLAS ORIGINALES DEL SISTEMA
-- =============================================

-- Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuario (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol VARCHAR(50) NOT NULL DEFAULT 'USER'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de Administradores  
CREATE TABLE IF NOT EXISTS administrador (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  rol VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
  departamento VARCHAR(100),
  estado_activo BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de Sismos (si no la tienes, agrégala)
CREATE TABLE IF NOT EXISTS sismos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  fecha DATETIME NOT NULL,
  magnitud DECIMAL(4,2) NOT NULL,
  latitud DECIMAL(10,7) NOT NULL,
  longitud DECIMAL(10,7) NOT NULL,
  profundidad DECIMAL(8,2),
  referencia VARCHAR(500),
  estado VARCHAR(10),
  hora_utc VARCHAR(20),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_fecha (fecha),
  INDEX idx_magnitud (magnitud),
  INDEX idx_estado (estado)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- TABLAS DEL DATAWAREHOUSE
-- =============================================

-- Tabla de dimensión: Zonas (Estados/Regiones)
CREATE TABLE IF NOT EXISTS dim_zonas (
    ID_zonas BIGINT AUTO_INCREMENT PRIMARY KEY,
    entidad VARCHAR(10) NOT NULL,
    nom_ent VARCHAR(100) NOT NULL,
    pobtot BIGINT DEFAULT 0,
    pobfem BIGINT DEFAULT 0,
    pobmas BIGINT DEFAULT 0,
    INDEX idx_nom_ent (nom_ent),
    INDEX idx_pobtot (pobtot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de dimensión: Economía
CREATE TABLE IF NOT EXISTS dim_economia (
    ID_economia BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre_entidad VARCHAR(100) NOT NULL,
    produccion_bruta_total DECIMAL(15,2) DEFAULT 0,
    insumos_utilizados DECIMAL(15,2) DEFAULT 0,
    consumo_intermedio DECIMAL(15,2) DEFAULT 0,
    valor_agregado DECIMAL(15,2) DEFAULT 0,
    formacion_capital DECIMAL(15,2) DEFAULT 0,
    activos_fijos_adquiridos DECIMAL(15,2) DEFAULT 0,
    INDEX idx_nombre_entidad (nombre_entidad),
    INDEX idx_produccion (produccion_bruta_total)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de dimensión: Tiempo  
CREATE TABLE IF NOT EXISTS dim_tiempo (
    ID_tiempo BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    hora_utc VARCHAR(20),
    anio INT NOT NULL,
    mes INT NOT NULL,
    dia INT NOT NULL,
    trimestre INT NOT NULL,
    INDEX idx_fecha (fecha),
    INDEX idx_anio (anio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla de hechos: Impacto de Sismos
CREATE TABLE IF NOT EXISTS fact_impacto_sismos_imputed (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ID_sismo BIGINT NOT NULL,
    ID_zonas BIGINT NOT NULL,
    ID_economia BIGINT NOT NULL,
    ID_tiempo BIGINT NOT NULL,
    poblacion_afectada BIGINT DEFAULT 0,
    impacto_economico DECIMAL(15,2) DEFAULT 0,
    determinante_riesgo DECIMAL(8,5) DEFAULT 0,
    riesgo_proporcional DECIMAL(10,5) DEFAULT 0,
    indice_zscore DECIMAL(8,5) DEFAULT 0,
    
    -- Índices para consultas rápidas
    INDEX idx_sismo (ID_sismo),
    INDEX idx_zona (ID_zonas),
    INDEX idx_economia (ID_economia),
    INDEX idx_tiempo (ID_tiempo),
    INDEX idx_riesgo (determinante_riesgo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tabla para logs de alertas críticas
CREATE TABLE IF NOT EXISTS log_alertas_criticas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(100),
    riesgo DECIMAL(8,5),
    descripcion TEXT,
    procesada BOOLEAN DEFAULT FALSE,
    INDEX idx_fecha (fecha),
    INDEX idx_estado (estado),
    INDEX idx_procesada (procesada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- INSERTAR DATOS INICIALES
-- =============================================

-- Insertar usuario administrador por defecto
INSERT IGNORE INTO usuario (nombre, email, password, rol) 
VALUES ('Admin', 'admin@example.com', '$2a$10$uS1TVzZvGW1lS.LiYJ0tR.amVTaX0qrK9zfkA3OMuGlHVcmEm7L02', 'ADMIN');
-- La contraseña es "password" en BCrypt

-- Insertar datos de zonas (estados de México)
INSERT IGNORE INTO dim_zonas (entidad, nom_ent, pobtot, pobfem, pobmas) VALUES
('01', 'Aguascalientes', 1425607, 731340, 694267),
('02', 'Baja California', 3769020, 1890891, 1878129),
('03', 'Baja California Sur', 798447, 407203, 391244),
('04', 'Campeche', 928363, 478526, 449837),
('05', 'Coahuila', 3146771, 1603143, 1543628),
('06', 'Colima', 731391, 369107, 362284),
('07', 'Chiapas', 5543828, 2819469, 2724359),
('08', 'Chihuahua', 3741869, 1896377, 1845492),
('09', 'Ciudad de Mexico', 9209944, 4739048, 4470896),
('10', 'Durango', 1832650, 937719, 894931),
('11', 'Guanajuato', 6166934, 3202500, 2964434),
('12', 'Guerrero', 3657048, 1857883, 1799165),
('13', 'Hidalgo', 3082841, 1617719, 1465122),
('14', 'Jalisco', 8348151, 4265052, 4083099),
('15', 'Mexico', 16992418, 8675860, 8316558),
('16', 'Michoacan', 4748846, 2442541, 2306305),
('17', 'Morelos', 1971520, 1020934, 950586),
('18', 'Nayarit', 1235456, 629866, 605590),
('19', 'Nuevo Leon', 5784442, 2905970, 2878472),
('20', 'Oaxaca', 4132148, 2167264, 1964884),
('21', 'Puebla', 6583278, 3396924, 3186354),
('22', 'Queretaro', 2368467, 1228168, 1140299),
('23', 'Quintana Roo', 1857985, 937993, 919992),
('24', 'San Luis Potosi', 2822255, 1459689, 1362566),
('25', 'Sinaloa', 3026943, 1537976, 1488967),
('26', 'Sonora', 2944840, 1482623, 1462217),
('27', 'Tabasco', 2402598, 1226392, 1176206),
('28', 'Tamaulipas', 3527735, 1803277, 1724458),
('29', 'Tlaxcala', 1342977, 700417, 642560),
('30', 'Veracruz', 8062579, 4165897, 3896682),
('31', 'Yucatan', 2320898, 1181477, 1139421),
('32', 'Zacatecas', 1622138, 833182, 788956);

-- Insertar datos económicos por estado
INSERT IGNORE INTO dim_economia (nombre_entidad, produccion_bruta_total, consumo_intermedio, valor_agregado, formacion_capital) VALUES
('Aguascalientes', 185000.50, 111000.30, 73999.20, 22500.00),
('Baja California', 520000.75, 312000.45, 207999.30, 63000.00),
('Baja California Sur', 95000.80, 57000.48, 37999.32, 11500.00),
('Campeche', 320000.60, 192000.36, 127999.24, 38800.00),
('Coahuila', 420000.85, 252000.51, 167999.34, 50900.00),
('Colima', 45000.80, 27000.48, 17999.32, 5500.00),
('Chiapas', 95000.40, 57000.24, 37999.16, 11500.00),
('Chihuahua', 480000.70, 288000.42, 191999.28, 58200.00),
('Ciudad de Mexico', 1250000.25, 750000.15, 499999.10, 151500.00),
('Durango', 145000.60, 87000.36, 57999.24, 17600.00),
('Guanajuato', 380000.90, 228000.54, 151999.36, 46000.00),
('Guerrero', 98000.75, 58000.45, 38999.30, 12000.00),
('Hidalgo', 165000.45, 99000.27, 65999.18, 20000.00),
('Jalisco', 650000.25, 390000.15, 259999.10, 78700.00),
('Mexico', 850000.90, 510000.54, 339999.36, 103000.00),
('Michoacan', 220000.60, 132000.36, 87999.24, 26700.00),
('Morelos', 125000.50, 75000.30, 49999.20, 15100.00),
('Nayarit', 85000.40, 51000.24, 33999.16, 10300.00),
('Nuevo Leon', 750000.85, 450000.51, 299999.34, 90900.00),
('Oaxaca', 125000.50, 75000.30, 49999.20, 15000.00),
('Puebla', 380000.70, 228000.42, 151999.28, 46000.00),
('Queretaro', 285000.60, 171000.36, 113999.24, 34500.00),
('Quintana Roo', 195000.80, 117000.48, 77999.32, 23600.00),
('San Luis Potosi', 185000.75, 111000.45, 73999.30, 22400.00),
('Sinaloa', 265000.90, 159000.54, 105999.36, 32100.00),
('Sonora', 320000.85, 192000.51, 127999.34, 38800.00),
('Tabasco', 285000.70, 171000.42, 113999.28, 34500.00),
('Tamaulipas', 385000.60, 231000.36, 153999.24, 46700.00),
('Tlaxcala', 65000.40, 39000.24, 25999.16, 7900.00),
('Veracruz', 420000.85, 252000.51, 167999.34, 50900.00),
('Yucatan', 185000.80, 111000.48, 73999.32, 22400.00),
('Zacatecas', 125000.75, 75000.45, 49999.30, 15100.00);

-- Insertar algunos sismos de ejemplo
INSERT IGNORE INTO sismos (fecha, magnitud, latitud, longitud, profundidad, referencia, estado, hora_utc) VALUES
('2024-01-15 14:30:00', 6.2, 17.0732, -96.7266, 35.5, '45 km al SO de Salina Cruz, OAX', 'OAX', '19:30:00'),
('2024-01-20 09:45:00', 4.8, 17.5506, -99.5024, 28.2, '32 km al NE de Acapulco, GRO', 'GRO', '15:45:00'),
('2024-02-05 16:20:00', 5.5, 20.6595, -103.3494, 42.1, '28 km al O de Guadalajara, JAL', 'JAL', '22:20:00'),
('2024-02-10 11:15:00', 7.1, 19.2465, -103.7248, 15.8, '15 km al SO de Colima, COL', 'COL', '17:15:00'),
('2024-02-15 13:50:00', 3.8, 19.5665, -101.7068, 38.4, '25 km al NE de Morelia, MICH', 'MICH', '19:50:00'),
('2024-03-01 08:25:00', 5.2, 16.7569, -93.1292, 52.3, '40 km al SE de Tuxtla Gutierrez, CHIS', 'CHIS', '14:25:00'),
('2024-03-10 18:40:00', 4.3, 19.1738, -96.1342, 25.7, '35 km al E de Veracruz, VER', 'VER', '00:40:00'),
('2024-03-15 06:55:00', 6.8, 18.8462, -104.2537, 45.2, '50 km al SO de Manzanillo, COL', 'COL', '12:55:00');

-- Insertar registros de tiempo correspondientes a los sismos
INSERT IGNORE INTO dim_tiempo (fecha, hora_utc, anio, mes, dia, trimestre) VALUES
('2024-01-15 14:30:00', '19:30:00', 2024, 1, 15, 1),
('2024-01-20 09:45:00', '15:45:00', 2024, 1, 20, 1),
('2024-02-05 16:20:00', '22:20:00', 2024, 2, 5, 1),
('2024-02-10 11:15:00', '17:15:00', 2024, 2, 10, 1),
('2024-02-15 13:50:00', '19:50:00', 2024, 2, 15, 1),
('2024-03-01 08:25:00', '14:25:00', 2024, 3, 1, 1),
('2024-03-10 18:40:00', '00:40:00', 2024, 3, 10, 1),
('2024-03-15 06:55:00', '12:55:00', 2024, 3, 15, 1);

-- Insertar registros en la tabla de hechos (relacionando sismos con zonas y economía)
INSERT IGNORE INTO fact_impacto_sismos_imputed 
(ID_sismo, ID_zonas, ID_economia, ID_tiempo, poblacion_afectada, impacto_economico, determinante_riesgo, riesgo_proporcional, indice_zscore) 
VALUES
(1, 20, 20, 1, 2500000, 125000.50, 0.85, 0.050, 1.8),  -- Oaxaca, magnitud 6.2
(2, 12, 12, 2, 1800000, 98000.75, 0.68, 0.054, 1.1),   -- Guerrero, magnitud 4.8
(3, 14, 14, 3, 4200000, 650000.25, 0.72, 0.155, 1.4),  -- Jalisco, magnitud 5.5
(4, 6, 6, 4, 365000, 45000.80, 0.92, 0.123, 2.1),      -- Colima, magnitud 7.1
(5, 16, 16, 5, 2400000, 220000.60, 0.45, 0.092, 0.8),  -- Michoacán, magnitud 3.8
(6, 7, 7, 6, 2800000, 95000.40, 0.78, 0.034, 1.5),     -- Chiapas, magnitud 5.2
(7, 30, 30, 7, 4000000, 420000.85, 0.53, 0.105, 0.9),  -- Veracruz, magnitud 4.3
(8, 6, 6, 8, 365000, 45000.80, 0.95, 0.123, 2.3);      -- Colima, magnitud 6.8

-- =============================================
-- VISTAS PARA ANÁLISIS
-- =============================================

-- Vista para estadísticas por estado
CREATE OR REPLACE VIEW vista_estadisticas_estados AS
SELECT 
    z.nom_ent as estado,
    COUNT(f.ID_sismo) as total_sismos,
    AVG(s.magnitud) as magnitud_promedio,
    MAX(s.magnitud) as magnitud_maxima,
    AVG(f.determinante_riesgo) as indice_riesgo_promedio,
    SUM(f.poblacion_afectada) as poblacion_afectada_total,
    SUM(f.impacto_economico) as impacto_economico_total,
    e.produccion_bruta_total,
    z.pobtot as poblacion_total
FROM fact_impacto_sismos_imputed f
INNER JOIN dim_zonas z ON f.ID_zonas = z.ID_zonas
INNER JOIN dim_economia e ON f.ID_economia = e.ID_economia
INNER JOIN sismos s ON f.ID_sismo = s.id
GROUP BY z.nom_ent, e.produccion_bruta_total, z.pobtot
ORDER BY AVG(f.determinante_riesgo) DESC;

-- Vista para verificar que todo funciona
CREATE OR REPLACE VIEW vista_test_completo AS
SELECT 
    s.id as sismo_id,
    s.magnitud,
    s.fecha,
    z.nom_ent as estado,
    e.produccion_bruta_total,
    f.poblacion_afectada,
    f.impacto_economico,
    f.determinante_riesgo
FROM sismos s
INNER JOIN fact_impacto_sismos_imputed f ON s.id = f.ID_sismo
INNER JOIN dim_zonas z ON f.ID_zonas = z.ID_zonas
INNER JOIN dim_economia e ON f.ID_economia = e.ID_economia
ORDER BY s.fecha DESC;

-- =============================================
-- CONSULTAS DE VERIFICACIÓN
-- =============================================

-- Mostrar resumen de tablas creadas
SELECT 'usuario' as tabla, COUNT(*) as registros FROM usuario
UNION ALL
SELECT 'administrador' as tabla, COUNT(*) as registros FROM administrador
UNION ALL
SELECT 'sismos' as tabla, COUNT(*) as registros FROM sismos
UNION ALL
SELECT 'dim_zonas' as tabla, COUNT(*) as registros FROM dim_zonas
UNION ALL
SELECT 'dim_economia' as tabla, COUNT(*) as registros FROM dim_economia  
UNION ALL
SELECT 'dim_tiempo' as tabla, COUNT(*) as registros FROM dim_tiempo
UNION ALL
SELECT 'fact_impacto_sismos_imputed' as tabla, COUNT(*) as registros FROM fact_impacto_sismos_imputed;