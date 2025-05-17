package com.example.deporsm.repository;

import com.example.deporsm.model.ComodidadInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComodidadInstalacionRepository extends JpaRepository<ComodidadInstalacion, Integer> {
    List<ComodidadInstalacion> findByInstalacionId(Integer instalacionId);
}
