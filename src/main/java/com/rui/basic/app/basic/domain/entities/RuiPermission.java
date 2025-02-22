package com.rui.basic.app.basic.domain.entities;

import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_PERMISSIONS")
@Data
@NoArgsConstructor
@ToString(exclude = {"roleId", "actionId"})
@EqualsAndHashCode(exclude = {"roleId", "actionId"})
public class RuiPermission implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_PERMISSIONS_SEQ")
    @SequenceGenerator(name = "RUI_PERMISSIONS_SEQ", sequenceName = "RUI_PERMISSIONS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "STATUS")
    private Short status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID")
    private RuiRoles roleId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTION_ID")
    private RuiActions actionId;
}
