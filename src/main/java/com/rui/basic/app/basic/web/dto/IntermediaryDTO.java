package com.rui.basic.app.basic.web.dto;

import java.io.Serializable;
import java.util.List;

import com.rui.basic.app.basic.domain.entities.RuiIntermediary;
import com.rui.basic.app.basic.domain.enums.IntermediaryState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntermediaryDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String radicateNumber;
    private String resolution;
    private String phone;
    private String idoneidadInfo;
    private IntermediaryState state;
    private String name;  // Nombre de la compañía o persona
    private String type;
    private PersonDTO person;
    private CompanyDTO company;
    private InfrastructureDTO infrastructure;
    private List<AssignmentDTO> assignments;
    private List<HistoryDTO> history;
    private String departmentName;
    

    // Constructor que coincide con la consulta JPQL
    public IntermediaryDTO(Long id, String radicateNumber, IntermediaryState state, 
                          String companyName, String companyEmail, String companyPhone,
                          String firstName, String firstSurname, String personEmail, 
                          String personPhone, String departmentName, String intermediaryType) {
        this.id = id;
        this.radicateNumber = radicateNumber;
        this.state = state;
        this.name = companyName != null ? companyName : (firstName + " " + firstSurname);
        this.phone = companyPhone != null ? companyPhone : personPhone;
        this.type = intermediaryType;
        this.departmentName = departmentName;
    }
    
    
    public static IntermediaryDTO fromEntity(RuiIntermediary intermediary) {
        IntermediaryDTO dto = new IntermediaryDTO();
        dto.setId(intermediary.getId());
        dto.setRadicateNumber(intermediary.getRadicateNumber());
        
        // Nombre de compañía o persona
        if (intermediary.getCompanyId() != null) {
            dto.setName(intermediary.getCompanyId().getName());
            // Obtener el nombre del departamento
            if (intermediary.getCompanyId().getDepartmentId() != null) {
                dto.setDepartmentName(intermediary.getCompanyId().getDepartmentId().getName());
            }
        } else if (intermediary.getPersonId() != null) {
            dto.setName(intermediary.getPersonId().getFirstName() + " " + 
                       intermediary.getPersonId().getFirstSurname());
        }
        
        // Tipo de intermediario
        if (intermediary.getTypeIntermediarieId() != null) {
            dto.setType(intermediary.getTypeIntermediarieId().getValue());
        }
        
        // Teléfono
        if (intermediary.getInfrastructureOperationalId() != null) {
            dto.setPhone(intermediary.getInfrastructureOperationalId().getPhone1());
        }
        
        
        
        return dto;
    }

}
