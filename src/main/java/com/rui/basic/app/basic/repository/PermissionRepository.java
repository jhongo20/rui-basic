package com.rui.basic.app.basic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.rui.basic.app.basic.domain.entities.RuiPermission;
import com.rui.basic.app.basic.domain.entities.RuiRoles;

public interface PermissionRepository extends JpaRepository<RuiPermission, Long> {

    @Query("SELECT p FROM RuiPermission p WHERE p.roleId = :roleId AND p.status = :status")
    List<RuiPermission> findAllByRoleAndStatus(
        @Param("roleId") RuiRoles roleId,
        @Param("status") Short status
    );

    void deleteAllByRoleId(RuiRoles roleId);

}
