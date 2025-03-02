package com.rui.basic.app.basic.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rui.basic.app.basic.domain.entities.RuiIdoniedad;
import com.rui.basic.app.basic.domain.entities.RuiInfraHuman;
import com.rui.basic.app.basic.domain.entities.RuiInfraOperational;
import com.rui.basic.app.basic.domain.entities.RuiSupport;
import com.rui.basic.app.basic.domain.entities.RuiWorkExperience;

@Repository
public interface RuiSupportRepository extends JpaRepository<RuiSupport, Long> {
    
    // Nuevo método para buscar por idoneidad ID
    // Modificar la consulta para verificar el status de la persona
    // Para DocumentService (con estado de persona)
    @Query("SELECT s FROM RuiSupport s " +
           "JOIN s.idoniedadId i " +
           "JOIN i.personId p " +
           "WHERE s.idoniedadId = :idoniedad " +
           "AND s.status = 1 " +
           "AND p.status = :activeStatus")
    Optional<RuiSupport> findByIdoniedadIdAndPersonStatus(
        @Param("idoniedad") RuiIdoniedad idoniedad,
        @Param("activeStatus") Integer activeStatus
    );

    // Para IdoneidadProfesionalService (solo por ID)
    @Query("SELECT s FROM RuiSupport s " +
           "WHERE s.idoniedadId.id = :idoniedadId " +
           "AND s.status = 1")
    Optional<RuiSupport> findByIdoniedadId(@Param("idoniedadId") Long idoniedadId);

    // Método para infraestructura humana
    Optional<RuiSupport> findByInfraHumnaId(RuiInfraHuman infraHumanaId);
    
    @Query("SELECT s FROM RuiSupport s WHERE s.filename = :filename AND s.status = :status")
    Optional<RuiSupport> findByFilenameAndStatus(@Param("filename") String filename, @Param("status") Short status);
    
    Optional<RuiSupport> findByRouteAndStatus(String route, Short status);

    Optional<RuiSupport> findByInfraOperationalSignAndStatus(RuiInfraOperational infraOperationalSign, Short status);
    Optional<RuiSupport> findByInfraOperationalSign(RuiInfraOperational infraOperational);

    // Método para obtener un único soporte activo
    @Query("SELECT s FROM RuiSupport s " +
           "WHERE s.workExperienceId = :workExperience " +
           "AND s.status = 1 " +
           "ORDER BY s.id DESC")
    Optional<RuiSupport> findFirstByWorkExperienceId(@Param("workExperience") RuiWorkExperience workExperience);

    // O si prefieres usar una consulta nativa
    @Query(value = "SELECT * FROM rui_support " +
           "WHERE work_experience_id = :workExperienceId " +
           "AND status = 1 " +
           "ORDER BY id DESC " +
           "FETCH FIRST 1 ROW ONLY", 
           nativeQuery = true)
    Optional<RuiSupport> findLatestByWorkExperienceId(@Param("workExperienceId") Long workExperienceId);
    
    // Método para buscar por experiencia laboral ID
    //Optional<RuiSupport> findByWorkExperienceId(RuiWorkExperience workExperienceId);
    //List<RuiSupport> findByWorkExperienceId(RuiWorkExperience workExperience);
    // Agregar una consulta personalizada para obtener el soporte más reciente
    @Query("SELECT s FROM RuiSupport s " +
           "WHERE s.workExperienceId = :workExperience " +
           "AND s.status = 1")
    List<RuiSupport> findByWorkExperienceId(@Param("workExperience") RuiWorkExperience workExperience);
    

    // Métodos para infraestructura operativa que retornan listas
    List<RuiSupport> findAllByInfraOperationalCc(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalSoft(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalHard(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalProce(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalSsl(RuiInfraOperational infraOperational);

    //--------------------------------------------------------------------------------

    
}
