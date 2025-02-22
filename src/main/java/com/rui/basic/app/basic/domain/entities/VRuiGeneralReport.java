package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_RUI_GENERAL_REPORT")
@Data
@NoArgsConstructor
public class VRuiGeneralReport implements Serializable {
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "CANTIDAD")
    private BigInteger cantidad;
    
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;
    
    @Column(name = "STATUS")
    private BigInteger status;
}
