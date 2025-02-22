package com.rui.basic.app.basic.repository;

import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuiIdoniedadRepository extends JpaRepository<RuiIdoniedad, Long> {
    @Query("SELECT i FROM RuiIdoniedad i " +
       "JOIN i.personId p " +
       "WHERE i.intermediaryId.id = :intermediaryId " +
       "AND i.status = 1 " +
       "AND p.status = 1")
List<RuiIdoniedad> findByIntermediaryId_Id(@Param("intermediaryId") Long intermediaryId);
    
    @Query("SELECT i FROM RuiIdoniedad i " +
       "JOIN i.personId p " +
       "WHERE i.intermediaryId.id = :intermediaryId " +
       "AND i.status = 1 " +  // Solo idoneidades activas
       "AND p.status = 1 " +  // Solo personas activas
       "ORDER BY i.dateCourse DESC")
List<RuiIdoniedad> findMostRecentByIntermediaryId(@Param("intermediaryId") Long intermediaryId);
}
