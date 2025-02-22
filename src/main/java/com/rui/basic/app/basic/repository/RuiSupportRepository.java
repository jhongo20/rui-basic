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
    Optional<RuiSupport> findByIdoniedadId(RuiIdoniedad idoniedadId);

    // Método para infraestructura humana
    Optional<RuiSupport> findByInfraHumnaId(RuiInfraHuman infraHumanaId);
    
    @Query("SELECT s FROM RuiSupport s WHERE s.filename = :filename AND s.status = :status")
    Optional<RuiSupport> findByFilenameAndStatus(@Param("filename") String filename, @Param("status") Short status);
    
    Optional<RuiSupport> findByRouteAndStatus(String route, Short status);

    Optional<RuiSupport> findByInfraOperationalSignAndStatus(RuiInfraOperational infraOperationalSign, Short status);
    Optional<RuiSupport> findByInfraOperationalSign(RuiInfraOperational infraOperational);

    
    // Método para buscar por experiencia laboral ID
    Optional<RuiSupport> findByWorkExperienceId(RuiWorkExperience workExperienceId);
    //List<RuiSupport> findByWorkExperienceId(RuiWorkExperience workExperience);
    // Agregar una consulta personalizada para obtener el soporte más reciente
    

    // Métodos para infraestructura operativa que retornan listas
    List<RuiSupport> findAllByInfraOperationalCc(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalSoft(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalHard(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalProce(RuiInfraOperational infraOperational);
    List<RuiSupport> findAllByInfraOperationalSsl(RuiInfraOperational infraOperational);

    
}
