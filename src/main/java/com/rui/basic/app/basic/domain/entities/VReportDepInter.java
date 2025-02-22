package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_REPORT_DEP_INTER")
@Data
@NoArgsConstructor
public class VReportDepInter implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "QTY")
    private BigInteger qty;
    
    @Size(max = 100)
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;
    
    @Column(name = "CANTIDAD")
    private BigInteger cantidad;
    
    @Column(name = "TYPE_INTERMEDIARIE")
    private BigInteger typeIntermediarie;
    
    @Size(max = 150)
    @Column(name = "INTERMEDIARIE_NAME")
    private String intermediarieName;
}
