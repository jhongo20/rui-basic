package com.rui.basic.app.basic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiCity;
import com.rui.basic.app.basic.domain.entities.RuiDepartment;

@Repository
public interface RuiCityRepository extends JpaRepository<RuiCity, Long> {
    
    List<RuiCity> findByDepartmentId_IdOrderByNameAsc(Long departmentId);
    
    List<RuiCity> findByDepartmentId(RuiDepartment department);
}