package com.example.deporsm.repository;

import com.example.deporsm.model.ConfiguracionGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionGeneralRepository extends JpaRepository<ConfiguracionGeneral, Integer> {
}
