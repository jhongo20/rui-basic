package com.rui.basic.app.basic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;
import com.rui.basic.app.basic.web.dto.IntermediaryHistoryDTO;

@Repository
public interface RuiIntermediaryHistoryRepository extends JpaRepository<RuiIntermediaryHistory, Long> {
    RuiIntermediaryHistory findByIntermediaryId(RuiIntermediary intermediary);
    
    //List<RuiIntermediaryHistory> findByIntermediaryIdOrderByDatetimeDesc(RuiIntermediary intermediary);
    @Query(value = "SELECT U.USERNAME " +
       "FROM RUI_USERS U " +
       "JOIN RUI_INTERMEDIARY_HISTORY H ON U.ID = H.FUNCTIONARY_ID " +
       "JOIN RUI_INTERMEDIARIES I ON H.INTERMEDIARY_ID = I.ID " +
       "WHERE I.ID = :intermediaryId",
       nativeQuery = true)
String findFunctionaryUsername(@Param("intermediaryId") Long intermediaryId);

}
