package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_RUI_CONSOLIDATED_REPORT")
@Data
@NoArgsConstructor
public class VRuiConsolidatedReport implements Serializable {
    
    @Id
    @Column(name = "ESTADO")
    private BigInteger estado;
    
    @Column(name = "CANTIDAD")
    private BigInteger cantidad;
    
    @Column(name = "PORCENTAJE")
    private BigInteger porcentaje;
}
