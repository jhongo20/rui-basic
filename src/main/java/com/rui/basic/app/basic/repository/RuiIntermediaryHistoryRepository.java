package com.rui.basic.app.basic.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;

@Repository
public interface RuiIntermediaryHistoryRepository extends JpaRepository<RuiIntermediaryHistory, Long> {
    RuiIntermediaryHistory findByIntermediaryId(RuiIntermediary intermediary);
    
    //List<RuiIntermediaryHistory> findByIntermediaryIdOrderByDatetimeDesc(RuiIntermediary intermediary);
    @Query(value = """
    SELECT u.USERNAME
    FROM RUI_USERS u
    INNER JOIN RUI_ASSIGNMENTS a ON u.ID = a.USER_ID
    WHERE a.ID = (
        SELECT MAX(a2.ID)
        FROM RUI_ASSIGNMENTS a2
        WHERE a2.INTERMEDIARY_ID = :intermediaryId
    )
    AND u.STATUS = 1
    """, nativeQuery = true)
    String findFunctionaryUsername(@Param("intermediaryId") Long intermediaryId);

    @Query("SELECT h FROM RuiIntermediaryHistory h WHERE h.intermediaryId.id = :intermediaryId AND h.status = 1")
    Optional<RuiIntermediaryHistory> findActiveByIntermediaryId(@Param("intermediaryId") Long intermediaryId);

}
