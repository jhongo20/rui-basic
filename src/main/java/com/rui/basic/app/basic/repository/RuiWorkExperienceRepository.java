package com.rui.basic.app.basic.repository;

import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RuiWorkExperienceRepository extends JpaRepository<RuiWorkExperience, Long> {
    @Query("SELECT we FROM RuiWorkExperience we " +
           "WHERE we.infraHumanId.id = :infraHumanId " +
           "AND we.status = 1")
    List<RuiWorkExperience> findByInfraHumanId_Id(@Param("infraHumanId") Long infraHumanId);

    @Query("SELECT we FROM RuiWorkExperience we " +
           "WHERE we.infraHumanId.id = :infraHumanId " +
           "AND we.status = :status " +
           "ORDER BY we.startDate DESC")
    List<RuiWorkExperience> findByInfraHumanIdAndStatus(
        @Param("infraHumanId") Long infraHumanId, 
        @Param("status") Short status);

        @Query("SELECT w FROM RuiWorkExperience w " +
        "WHERE w.infraHumanId.intermediary.id = :intermediaryId " +
        "AND w.status = 1")
    List<RuiWorkExperience> findByInfraHumanId_Intermediary_Id(@Param("intermediaryId") Long intermediaryId);
       
}
