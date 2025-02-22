package com.rui.basic.app.basic.domain.entities;



import java.io.Serializable;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "RUI_USERS")
@Data
@NoArgsConstructor
@ToString(exclude = {"roleId", "person"})
@EqualsAndHashCode(exclude = {"roleId", "person"})
public class RuiUser implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RUI_USERS_SEQ")
    @SequenceGenerator(name = "RUI_USERS_SEQ", sequenceName = "RUI_USERS_SEQUENCE", allocationSize = 1)
    private Long id;
    
    @Column(name = "USERNAME", unique = true)
    private String username;
    
    @Column(name = "PASSWORD")
    private String password;
    
    @Column(name = "STATUS")
    private Integer status;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ROLE_ID")
    private RuiRoles roleId;;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "PERSON_ID")
    private RuiPerson person;

}
