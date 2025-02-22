package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_TRANSACTIONS")
@Data
@NoArgsConstructor
@ToString(exclude = {"userId"})
@EqualsAndHashCode(exclude = {"userId"})
public class RuiTransaction implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_TRANSACTIONS_SEQ")
    @SequenceGenerator(name = "RUI_TRANSACTIONS_SEQ", sequenceName = "RUI_TRANSACTIONS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "TRANSACTION_TYPE")
    private String transactionType;
    
    @Column(name = "DESCRIPTION")
    private String description;
    
    @Column(name = "TABLE_NAME")
    private String tableName;
    
    @Column(name = "TABLE_ID")
    private Long tableId;
    
    @Column(name = "TRANSACTION_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private RuiUser userId;
}
