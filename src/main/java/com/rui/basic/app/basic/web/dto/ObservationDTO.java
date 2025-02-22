package com.rui.basic.app.basic.web.dto;

import lombok.Data;

@Data
public class ObservationDTO {
    private Long id;
    private String fieldName;
    private String observation;
    private String tableName;
    private Long tableId;
    private Long intermediaryHistoryId;
}
