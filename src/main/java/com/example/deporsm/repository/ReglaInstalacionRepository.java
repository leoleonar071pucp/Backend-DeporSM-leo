package com.example.deporsm.repository;

import com.example.deporsm.model.ReglaInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReglaInstalacionRepository extends JpaRepository<ReglaInstalacion, Integer> {
    List<ReglaInstalacion> findByInstalacionId(Integer instalacionId);
}
