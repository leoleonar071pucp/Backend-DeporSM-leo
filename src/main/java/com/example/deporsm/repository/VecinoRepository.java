package com.example.deporsm.repository;

import com.example.deporsm.dto.VecinoDTO;
import com.example.deporsm.dto.projections.VecinoDTOProjection;
import com.example.deporsm.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VecinoRepository extends JpaRepository<Usuario, Integer> {    @Query(value = """
        SELECT 
            u.id as id,
            u.nombre as nombre,
            u.apellidos as apellidos,
            u.email as email,
            u.telefono as telefono,
            u.direccion as direccion,
            u.dni as dni,
            u.activo as activo,
            DATE_FORMAT(u.last_login, '%Y-%m-%d %H:%i:%s') as lastLogin,
            COUNT(r.id) AS reservas
        FROM 
            deportes_sm.usuarios u
        LEFT JOIN 
            deportes_sm.reservas r ON u.id = r.usuario_id
        WHERE 
            u.role_id = 4
        GROUP BY 
            u.id, u.nombre, u.apellidos, u.email, u.telefono, u.direccion, u.dni, u.activo, u.last_login
    """, nativeQuery = true)
    List<VecinoDTOProjection> findAllVecinos();    @Query(value = """
        SELECT 
            u.id as id,
            u.nombre as nombre,
            u.apellidos as apellidos,
            u.email as email,
            u.telefono as telefono,
            u.direccion as direccion,
            u.dni as dni,
            u.activo as activo,
            DATE_FORMAT(u.last_login, '%Y-%m-%d %H:%i:%s') as lastLogin,
            COUNT(r.id) AS reservas
        FROM 
            deportes_sm.usuarios u
        LEFT JOIN 
            deportes_sm.reservas r ON u.id = r.usuario_id
        WHERE 
            u.role_id = 4 AND
            (LOWER(CONCAT(u.nombre, ' ', u.apellidos)) LIKE LOWER(CONCAT('%', :query, '%')) OR 
             LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR 
             u.dni LIKE CONCAT('%', :query, '%'))
        GROUP BY 
            u.id, u.nombre, u.apellidos, u.email, u.telefono, u.direccion, u.dni, u.activo, u.last_login
    """, nativeQuery = true)
    List<VecinoDTOProjection> buscarVecinos(@Param("query") String query);    @Query(value = """
        SELECT 
            u.id as id,
            u.nombre as nombre,
            u.apellidos as apellidos,
            u.email as email,
            u.telefono as telefono,
            u.direccion as direccion,
            u.dni as dni,
            u.activo as activo,
            DATE_FORMAT(u.last_login, '%Y-%m-%d %H:%i:%s') as lastLogin,
            COUNT(r.id) AS reservas
        FROM 
            deportes_sm.usuarios u
        LEFT JOIN 
            deportes_sm.reservas r ON u.id = r.usuario_id
        WHERE
            u.role_id = 4 AND
            u.activo = :activo
        GROUP BY
            u.id, u.nombre, u.apellidos, u.email, u.telefono, u.direccion, u.dni, u.activo, u.last_login
    """, nativeQuery = true)
    List<VecinoDTOProjection> findByActivo(@Param("activo") boolean activo);
}
