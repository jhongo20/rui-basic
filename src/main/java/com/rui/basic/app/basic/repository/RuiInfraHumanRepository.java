package com.rui.basic.app.basic.repository;

import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuiInfraHumanRepository extends JpaRepository<RuiInfraHuman, Long> {
    @Query("SELECT ih FROM RuiInfraHuman ih JOIN ih.intermediary i WHERE i.id = :intermediaryId")
    Optional<RuiInfraHuman> findByIntermediaryId(@Param("intermediaryId") Long intermediaryId);
    
    // Método alternativo si el anterior sigue dando problemas
    @Query(value = "SELECT ih.* FROM RUI_INFRA_HUMAN ih " +
           "JOIN RUI_INTERMEDIARIES i ON ih.ID = i.INFRASTRUCTURE_HUMAN_ID " +
           "WHERE i.ID = :intermediaryId", nativeQuery = true)
    Optional<RuiInfraHuman> findByIntermediaryIdNative(@Param("intermediaryId") Long intermediaryId);
}
