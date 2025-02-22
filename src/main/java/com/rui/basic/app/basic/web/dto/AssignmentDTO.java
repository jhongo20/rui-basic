package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignmentDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private UserDTO user;
    private IntermediaryDTO intermediary;
    private Short status;
    private Date assignmentDate;
}
