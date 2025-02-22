package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_RUI_REPORT_DETAILS")
@Data
@NoArgsConstructor
public class VRuiReportDetails implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "QTY")
    private BigInteger qty;
    
    @Column(name = "CANTIDAD")
    private BigInteger cantidad;
    
    @Size(max = 100)
    @Column(name = "DEPARTMENT_NAME")
    private String departmentName;
    
    @Column(name = "STATEDEP")
    private BigInteger statedep;
    
    @Size(max = 100)
    @Column(name = "STATUS_NAME")
    private String statusName;
}
