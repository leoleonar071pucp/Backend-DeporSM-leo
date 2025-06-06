package com.example.deporsm.repository;

import com.example.deporsm.model.ConfiguracionSeguridad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionSeguridadRepository extends JpaRepository<ConfiguracionSeguridad, Integer> {
}
