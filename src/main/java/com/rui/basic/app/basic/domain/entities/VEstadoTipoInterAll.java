package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_ESTADO_TIPO_INTER_ALL")
@Data
@NoArgsConstructor
public class VEstadoTipoInterAll implements Serializable {
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "QTY")
    private BigInteger qty;
    
    @Column(name = "STATUS")
    private BigInteger status;
    
    @Column(name = "STATUS_NAME")
    private String statusName;
    
    @Column(name = "TYPE_INTERMEDIARIE")
    private BigInteger typeIntermediarie;
    
    @Column(name = "INTERMEDIARIE_NAME")
    private String intermediarieName;
}
