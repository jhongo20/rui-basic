package com.rui.basic.app.basic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiGenerics;

@Repository
public interface RuiGenericsRepository extends JpaRepository<RuiGenerics, Long> {
    
    @Query("SELECT g FROM RuiGenerics g WHERE g.fatherId = :fatherId AND g.status = :status")
    List<RuiGenerics> findByFatherIdAndStatus(
        @Param("fatherId") Long fatherId, 
        @Param("status") Short status
    );
    
    @Query("SELECT g FROM RuiGenerics g WHERE g.value = :value AND g.status = :status")
    List<RuiGenerics> findByValueAndStatus(
        @Param("value") String value, 
        @Param("status") Short status
    );

    // También podrías usar métodos derivados de Spring Data JPA:
    List<RuiGenerics> findByFatherIdAndStatusOrderByValueAsc(Long fatherId, Short status);
    
    List<RuiGenerics> findByValueContainingAndStatus(String value, Short status);
    
    // Para obtener un valor específico
    Optional<RuiGenerics> findByValueAndStatusAndFatherId(String value, Short status, Long fatherId);
}
