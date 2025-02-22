package com.rui.basic.app.basic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiDepartment;

@Repository
public interface RuiDepartmentRepository extends JpaRepository<RuiDepartment, Long> {
    
    List<RuiDepartment> findAllByOrderByNameAsc();
    
    @Query("SELECT d FROM RuiDepartment d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<RuiDepartment> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Métodos adicionales que podrían ser útiles
        
    // Para búsqueda por código ignorando mayúsculas/minúsculas
}
