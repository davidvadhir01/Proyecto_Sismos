// DimZonaRepository.java
package com.spring.proyectofinal.repository.datawarehouse;

import com.spring.proyectofinal.model.datawarehouse.DimZona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DimZonaRepository extends JpaRepository<DimZona, Long> {
    
    @Query("SELECT z FROM DimZona z ORDER BY z.pobtot DESC")
    List<DimZona> findByPoblacionDesc();
    
    @Query("""
        SELECT z.nomEnt, z.pobtot, z.pobfem, z.pobmas,
               (z.pobfem * 100.0 / z.pobtot) as porcentajeMujeres
        FROM DimZona z
        WHERE z.pobtot > :minPoblacion
        ORDER BY z.pobtot DESC
    """)
    List<Object[]> getEstadisticasPoblacionales(Long minPoblacion);
}
