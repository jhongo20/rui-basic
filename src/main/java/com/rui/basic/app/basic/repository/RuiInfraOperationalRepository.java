package com.rui.basic.app.basic.repository;


import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RuiInfraOperationalRepository extends JpaRepository<RuiInfraOperational, Long> {
    @Query("SELECT io FROM RuiInfraOperational io JOIN io.intermediary i WHERE i.id = :intermediaryId")
    Optional<RuiInfraOperational> findByIntermediaryId(@Param("intermediaryId") Long intermediaryId);
    
    // Alternativa con consulta nativa si la JPQL da problemas
    @Query(value = "SELECT io.* FROM RUI_INFRA_OPERATIONAL io " +
           "JOIN RUI_INTERMEDIARIES i ON io.ID = i.OPERATIONAL_INFRA_ID " +
           "WHERE i.ID = :intermediaryId", nativeQuery = true)
    Optional<RuiInfraOperational> findByIntermediaryIdNative(@Param("intermediaryId") Long intermediaryId);
}
