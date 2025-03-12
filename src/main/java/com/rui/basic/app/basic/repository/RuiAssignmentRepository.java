package com.rui.basic.app.basic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiAssignment;

@Repository
public interface RuiAssignmentRepository extends JpaRepository<RuiAssignment, Long> {
    // Métodos personalizados si son necesarios
}