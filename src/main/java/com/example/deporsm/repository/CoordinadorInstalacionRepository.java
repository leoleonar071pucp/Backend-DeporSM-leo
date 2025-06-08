package com.example.deporsm.repository;

import com.example.deporsm.model.CoordinadorInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordinadorInstalacionRepository extends JpaRepository<CoordinadorInstalacion, Integer> {

    List<CoordinadorInstalacion> findByUsuarioId(Integer usuarioId);

    List<CoordinadorInstalacion> findByInstalacionId(Integer instalacionId);

    @Query("SELECT ci FROM CoordinadorInstalacion ci WHERE ci.usuario.id = :usuarioId AND ci.instalacion.id = :instalacionId")
    CoordinadorInstalacion findByUsuarioIdAndInstalacionId(Integer usuarioId, Integer instalacionId);
}
