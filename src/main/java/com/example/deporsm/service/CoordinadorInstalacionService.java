package com.example.deporsm.service;

import com.example.deporsm.dto.CoordinadorAsignacionDTO;
import com.example.deporsm.dto.HorarioCoordinadorRequestDTO;
import com.example.deporsm.model.CoordinadorInstalacion;
import com.example.deporsm.model.HorarioCoordinador;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.CoordinadorInstalacionRepository;
import com.example.deporsm.repository.HorarioCoordinadorRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Service
public class CoordinadorInstalacionService {

    @Autowired
    private CoordinadorInstalacionRepository coordinadorInstalacionRepository;

    @Autowired
    private HorarioCoordinadorRepository horarioCoordinadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    /**
     * Asigna instalaciones y horarios a un coordinador
     */
    @Transactional
    public void asignarInstalacionesYHorarios(CoordinadorAsignacionDTO asignacionDTO) {
        // Verificar que el coordinador existe
        Usuario coordinador = usuarioRepository.findById(asignacionDTO.getCoordinadorId())
                .orElseThrow(() -> new RuntimeException("Coordinador no encontrado"));

        // Verificar que el usuario es coordinador
        if (coordinador.getRol().getId() != 3) {
            throw new RuntimeException("El usuario no es un coordinador");
        }

        // Eliminar asignaciones existentes del coordinador
        List<CoordinadorInstalacion> asignacionesExistentes =
            coordinadorInstalacionRepository.findByUsuarioId(asignacionDTO.getCoordinadorId());

        for (CoordinadorInstalacion asignacion : asignacionesExistentes) {
            // Eliminar horarios asociados
            List<HorarioCoordinador> horariosExistentes =
                horarioCoordinadorRepository.findByCoordinadorInstalacionId(asignacion.getId());
            horarioCoordinadorRepository.deleteAll(horariosExistentes);
        }

        // Forzar la ejecución de las eliminaciones de horarios
        horarioCoordinadorRepository.flush();

        // Eliminar las asignaciones
        coordinadorInstalacionRepository.deleteAll(asignacionesExistentes);

        // IMPORTANTE: Forzar la ejecución de las eliminaciones antes de las inserciones
        // para evitar violaciones de la constraint unique_coordinador_instalacion
        coordinadorInstalacionRepository.flush();

        // Crear nuevas asignaciones
        for (Integer instalacionId : asignacionDTO.getInstalacionIds()) {
            Instalacion instalacion = instalacionRepository.findById(instalacionId)
                    .orElseThrow(() -> new RuntimeException("Instalación no encontrada: " + instalacionId));

            CoordinadorInstalacion nuevaAsignacion = new CoordinadorInstalacion();
            nuevaAsignacion.setUsuario(coordinador);
            nuevaAsignacion.setInstalacion(instalacion);
            nuevaAsignacion.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            nuevaAsignacion.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

            CoordinadorInstalacion asignacionGuardada = coordinadorInstalacionRepository.save(nuevaAsignacion);

            // Crear horarios para esta asignación
            for (HorarioCoordinadorRequestDTO horarioDTO : asignacionDTO.getHorarios()) {
                if (horarioDTO.getInstalacionId().equals(instalacionId)) {
                    HorarioCoordinador nuevoHorario = new HorarioCoordinador();
                    nuevoHorario.setCoordinadorInstalacion(asignacionGuardada);
                    nuevoHorario.setDiaSemana(horarioDTO.getDiaSemana());
                    nuevoHorario.setHoraInicio(Time.valueOf(horarioDTO.getHoraInicio() + ":00"));
                    nuevoHorario.setHoraFin(Time.valueOf(horarioDTO.getHoraFin() + ":00"));
                    nuevoHorario.setCreatedAt(new Timestamp(System.currentTimeMillis()));
                    nuevoHorario.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

                    horarioCoordinadorRepository.save(nuevoHorario);
                }
            }
        }
    }

    /**
     * Elimina todas las asignaciones de un coordinador
     */
    @Transactional
    public void eliminarAsignacionesCoordinador(Integer coordinadorId) {
        List<CoordinadorInstalacion> asignaciones =
            coordinadorInstalacionRepository.findByUsuarioId(coordinadorId);

        for (CoordinadorInstalacion asignacion : asignaciones) {
            // Eliminar horarios asociados
            List<HorarioCoordinador> horarios =
                horarioCoordinadorRepository.findByCoordinadorInstalacionId(asignacion.getId());
            horarioCoordinadorRepository.deleteAll(horarios);
        }

        // Eliminar las asignaciones
        coordinadorInstalacionRepository.deleteAll(asignaciones);
    }

    /**
     * Obtiene las instalaciones asignadas a un coordinador
     */
    public List<CoordinadorInstalacion> obtenerAsignacionesPorCoordinador(Integer coordinadorId) {
        return coordinadorInstalacionRepository.findByUsuarioId(coordinadorId);
    }
}
