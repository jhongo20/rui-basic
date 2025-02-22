package com.rui.basic.app.basic.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiUser;
import com.rui.basic.app.basic.web.dto.RoleCountDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface RuiUserRepository extends JpaRepository<RuiUser, Long> {
    @Query("SELECT u FROM RuiUser u LEFT JOIN FETCH u.roleId " +
    "WHERE LOWER(u.username) = LOWER(:username) AND u.status = 1")
    Optional<RuiUser> findByUsername(@Param("username") String username);

    // Método adicional para verificar duplicados
    @Query("SELECT COUNT(u) FROM RuiUser u WHERE LOWER(u.username) = LOWER(:username)")
    int countByUsername(@Param("username") String username);
    
    @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM RuiUser u WHERE LOWER(u.username) = LOWER(:username)")
    boolean existsByUsername(@Param("username") String username);
    
    @Query(value = "SELECT * FROM rui_users WHERE LOWER(username) = LOWER(:username)", nativeQuery = true)
    Optional<RuiUser> findByUsernameNative(@Param("username") String username);
    Optional<RuiUser> findByPersonDocumentNumber(String documentNumber);

    @Query("SELECT COUNT(u) FROM RuiUser u WHERE u.status = :status")
    long countByStatus(@Param("status") Integer status);

    List<RuiUser> findByStatus(Integer status);
    
    List<RuiUser> findByRoleIdName(String roleName);
    
    List<RuiUser> findByRoleIdNameAndStatus(String roleName, Integer status);
    
    @Query("SELECT NEW com.rui.basic.app.basic.web.dto.RoleCountDto(r.name, COUNT(u)) " +
           "FROM RuiUser u JOIN u.roleId r GROUP BY r.name")
    List<RoleCountDto> countUsersByRole();

    //-----
    Optional<RuiUser> findByUsernameAndStatus(String username, Short status);

    @Query("SELECT u FROM RuiUser u WHERE u.status = :status AND u.roleId.id != 1")
    List<RuiUser> findAllOfficials(@Param("status") Integer status);
    
    
}
