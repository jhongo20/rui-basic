package com.rui.basic.app.basic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiCompany;

@Repository
public interface CompanyRepository extends JpaRepository<RuiCompany, Long> {
    
    Optional<RuiCompany> findByNit(String nit);
    
    @Query("SELECT c FROM RuiCompany c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RuiCompany> findByNameContainingIgnoreCase(@Param("name") String name);
}
