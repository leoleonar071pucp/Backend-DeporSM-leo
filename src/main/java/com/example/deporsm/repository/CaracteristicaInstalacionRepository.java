package com.example.deporsm.repository;

import com.example.deporsm.model.CaracteristicaInstalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaracteristicaInstalacionRepository extends JpaRepository<CaracteristicaInstalacion, Integer> {
    List<CaracteristicaInstalacion> findByInstalacionId(Integer instalacionId);
}
