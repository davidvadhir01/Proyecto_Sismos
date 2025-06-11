// DimEconomiaRepository.java
package com.spring.proyectofinal.repository.datawarehouse;

import com.spring.proyectofinal.model.datawarehouse.DimEconomia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DimEconomiaRepository extends JpaRepository<DimEconomia, Long> {
    
    @Query("""
        SELECT e FROM DimEconomia e 
        ORDER BY e.produccionBrutaTotal DESC
    """)
    List<DimEconomia> findByProduccionDesc();
    
    @Query("""
        SELECT e.nombreEntidad,
               e.produccionBrutaTotal,
               e.consumoIntermedio,
               (e.produccionBrutaTotal - e.consumoIntermedio) as excedente,
               (e.produccionBrutaTotal / e.consumoIntermedio) as eficiencia
        FROM DimEconomia e
        WHERE e.produccionBrutaTotal > e.consumoIntermedio
        ORDER BY (e.produccionBrutaTotal - e.consumoIntermedio) DESC
    """)
    List<Object[]> getEstadosSuperavitarios();
}