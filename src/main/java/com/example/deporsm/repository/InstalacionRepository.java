package com.example.deporsm.repository;

import com.example.deporsm.model.Instalacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstalacionRepository extends JpaRepository<Instalacion, Integer> {

    List<Instalacion> findByTipo(String tipo);

    List<Instalacion> findByActivo(Boolean activo);

    List<Instalacion> findByNombreContainingIgnoreCase(String nombre);

    List<Instalacion> findByNombreContainingIgnoreCaseAndActivo(String nombre, Boolean activo);

    List<Instalacion> findByTipoAndActivo(String tipo, Boolean activo);

    @Query("SELECT i FROM Instalacion i WHERE LOWER(i.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(i.tipo) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Instalacion> autocompleteByNombreOrTipo(@Param("query") String query);

    @Query("SELECT i FROM Instalacion i WHERE LOWER(i.ubicacion) LIKE LOWER(CONCAT('%', :ubicacion, '%'))")
    List<Instalacion> buscarPorUbicacion(@Param("ubicacion") String ubicacion);
}
