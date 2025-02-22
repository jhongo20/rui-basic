package com.rui.basic.app.basic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;
import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;

@Repository
public interface IntermediaryRepository extends JpaRepository<RuiIntermediary, Long> {
    
    @Query("SELECT i FROM RuiIntermediary i WHERE i.state = :state")
    List<RuiIntermediary> findAllByState(@Param("state") IntermediaryState state);
    
    Optional<RuiIntermediary> findByRadicateNumber(String radicateNumber);
    
    @Query("SELECT i FROM RuiIntermediary i WHERE i.personId.documentType = :documentType " +
           "AND i.personId.documentNumber = :documentNumber")
    Optional<RuiIntermediary> findByPersonDocument(
        @Param("documentType") RuiGenerics documentType,
        @Param("documentNumber") String documentNumber
    );
    
    @Query("SELECT i FROM RuiIntermediary i WHERE i.companyId.nit = :nit")
    Optional<RuiIntermediary> findByCompanyNit(@Param("nit") String nit);
}
