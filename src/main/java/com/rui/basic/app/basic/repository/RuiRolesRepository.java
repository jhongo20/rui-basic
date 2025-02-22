package com.rui.basic.app.basic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiRoles;

@Repository
public interface RuiRolesRepository extends JpaRepository<RuiRoles, Long> {
    Optional<RuiRoles> findByName(String name);
    List<RuiRoles> findByStatus(Integer status);

    //--
    @Query("SELECT r FROM RuiRoles r WHERE r.status = :status")
    List<RuiRoles> findAllActive(@Param("status") Short status);
    
    Optional<RuiRoles> findByNameAndStatus(String name, Short status);
    
    boolean existsByNameAndStatus(String name, Short status);
}
