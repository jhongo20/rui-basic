package com.rui.basic.app.basic.repository;

import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuiWorkExperienceRepository extends JpaRepository<RuiWorkExperience, Long> {
    List<RuiWorkExperience> findByInfraHumanId_Id(Long infraHumanId);
}
