package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_RUI_STATUS_REPORT")
@Data
@NoArgsConstructor
public class VRuiStatusReport implements Serializable {
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "STATUS")
    private BigInteger status;
    
    @Column(name = "CANTIDAD")
    private BigInteger cantidad;
    
    @Column(name = "STATUS_NAME")
    private String statusName;
}
