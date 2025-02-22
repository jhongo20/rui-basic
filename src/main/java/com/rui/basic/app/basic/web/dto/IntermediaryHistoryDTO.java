package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class IntermediaryHistoryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Date date;
    private String observation;
    private Short state;
    private UserDTO user;
    private IntermediaryDTO intermediary;
    private List<HistoryDTO> historyDetails;

    // Constructor para la proyección JPA
    public IntermediaryHistoryDTO(Long id, Date date, String observation, 
                                 Short state, UserDTO user, 
                                 IntermediaryDTO intermediary,
                                 List<HistoryDTO> historyDetails) {
        this.id = id;
        this.date = date;
        this.observation = observation;
        this.state = state;
        this.user = user;
        this.intermediary = intermediary;
        this.historyDetails = historyDetails;
    }
}
