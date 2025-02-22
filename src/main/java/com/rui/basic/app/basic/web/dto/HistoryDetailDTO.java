package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryDetailDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String fieldName;
    private String observation;
    private String tableName;
    private Long tableId;
    private IntermediaryHistoryDTO intermediaryHistory;
}
