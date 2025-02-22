package com.rui.basic.app.basic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiHistoryDetails;
import com.rui.basic.app.basic.domain.entities.RuiIntermediaryHistory;
import com.rui.basic.app.basic.web.dto.IntermediaryHistoryDTO;

@Repository
public interface RuiHistoryDetailsRepository extends JpaRepository<RuiHistoryDetails, Long> {
    Long countByIntermediaryHistoryId_IntermediaryId_Id(Long intermediaryId);
    
    List<RuiHistoryDetails> findByIntermediaryHistoryId(RuiIntermediaryHistory history);
    
    @Query("SELECT h FROM RuiHistoryDetails h WHERE h.intermediaryHistoryId = :history AND h.tableName = :tableName")
    List<RuiHistoryDetails> findByHistoryAndTableName(@Param("history") RuiIntermediaryHistory history, 
                                                     @Param("tableName") String tableName);

                                                     @Query("SELECT NEW com.rui.basic.app.basic.web.dto.IntermediaryHistoryDTO(" +
       "h.id, h.datetime, h.observation, h.status, " +
       "NEW com.rui.basic.app.basic.web.dto.UserDTO(h.functionaryId.id, h.functionaryId.username), " +
       "null, null) " +
       "FROM RuiIntermediaryHistory h " +
       "WHERE h.intermediaryId.id = :intermediaryId " +
       "ORDER BY h.datetime DESC")
    List<IntermediaryHistoryDTO> findLastHistoryByIntermediaryId(@Param("intermediaryId") Long intermediaryId);
}
