// FactImpactoSismosRepository.java
package com.spring.proyectofinal.repository.datawarehouse;

import com.spring.proyectofinal.model.datawarehouse.FactImpactoSismos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FactImpactoSismosRepository extends JpaRepository<FactImpactoSismos, Long> {
    
    @Query("""
        SELECT z.nomEnt as estado, 
               COUNT(f.idSismo) as totalSismos,
               AVG(f.determinanteRiesgo) as indiceRiesgo,
               SUM(f.poblacionAfectada) as poblacionAfectada,
               SUM(f.impactoEconomico) as impactoEconomico
        FROM FactImpactoSismos f
        JOIN f.zona z
        GROUP BY z.nomEnt
        ORDER BY AVG(f.determinanteRiesgo) DESC
    """)
    List<Object[]> getEstadisticasPorEstado();
    
    @Query("""
        SELECT f FROM FactImpactoSismos f
        JOIN f.zona z
        WHERE z.nomEnt = :estado
        ORDER BY f.determinanteRiesgo DESC
    """)
    List<FactImpactoSismos> findByEstado(@Param("estado") String estado);
    
    @Query("""
        SELECT z.nomEnt as estado,
               COUNT(f.idSismo) as cantidad,
               AVG(f.determinanteRiesgo) as riesgoPromedio
        FROM FactImpactoSismos f
        JOIN f.zona z
        WHERE f.determinanteRiesgo > :umbralRiesgo
        GROUP BY z.nomEnt
        ORDER BY AVG(f.determinanteRiesgo) DESC
    """)
    List<Object[]> getZonasAltoRiesgo(@Param("umbralRiesgo") Double umbralRiesgo);
    
    @Query("""
        SELECT t.anio,
               COUNT(f.idSismo) as cantidad,
               AVG(f.impactoEconomico) as impactoPromedio
        FROM FactImpactoSismos f
        JOIN f.tiempo t
        GROUP BY t.anio
        ORDER BY t.anio
    """)
    List<Object[]> getTendenciaTemporal();
}