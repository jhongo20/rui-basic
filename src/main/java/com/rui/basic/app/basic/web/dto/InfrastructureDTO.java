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
public class InfrastructureDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private HumanInfraDTO human;
    private OperationalInfraDTO operational;
}
