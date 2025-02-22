package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "V_RUI_REQUEST")
@Data
@NoArgsConstructor
public class VRuiRequest implements Serializable {
    
    @Id
    @Column(name = "ID")
    private BigInteger id;
    
    @Column(name = "DOCUMENT_NUMBER")
    private String documentNumber;
    
    @Column(name = "FULL_NAME")
    private String fullName;
    
    @Column(name = "STATUS")
    private BigInteger status;
    
    @Column(name = "OFFICIAL_ID")
    private BigInteger officialId;
    
    @Column(name = "ASSIGNMENT_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignmentDate;
}
