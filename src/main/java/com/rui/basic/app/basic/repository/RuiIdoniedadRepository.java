package com.rui.basic.app.basic.repository;

import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuiIdoniedadRepository extends JpaRepository<RuiIdoniedad, Long> {
    List<RuiIdoniedad> findByIntermediaryId_Id(Long intermediaryId);
    
    @Query(value = 
        "SELECT * FROM (" +
        "   SELECT i.*, " +
        "   ROW_NUMBER() OVER (PARTITION BY i.PERSON_ID ORDER BY i.DATE_COURSE DESC) as rn " +
        "   FROM RUI_IDONIEDAD i " +
        "   WHERE i.INTERMEDIARY_ID = :intermediaryId" +
        ") WHERE rn = 1 " +
        "ORDER BY DATE_COURSE DESC", 
        nativeQuery = true)
    List<RuiIdoniedad> findMostRecentByIntermediaryId(@Param("intermediaryId") Long intermediaryId);
}
