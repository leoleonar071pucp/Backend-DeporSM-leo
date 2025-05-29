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
import com.example.deporsm.service.NotificacionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CoordinadorInstalacionService {

    @Autowired
    private CoordinadorInstalacionRepository coordinadorInstalacionRepository;

    @Autowired
    private HorarioCoordinadorRepository horarioCoordinadorRepository;

    @Autowired
    private NotificacionService notificacionService;

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

        // Obtener instalaciones actualmente asignadas al coordinador
        List<CoordinadorInstalacion> asignacionesExistentes =
            coordinadorInstalacionRepository.findByUsuarioId(asignacionDTO.getCoordinadorId());

        // Crear un Set con los IDs de instalaciones existentes para comparación rápida
        Set<Integer> instalacionesExistentesIds = asignacionesExistentes.stream()
            .map(asignacion -> asignacion.getInstalacion().getId())
            .collect(Collectors.toSet());

        // Identificar instalaciones nuevas (que no estaban asignadas antes)
        Set<Integer> nuevasInstalacionesIds = asignacionDTO.getInstalacionIds().stream()
            .filter(id -> !instalacionesExistentesIds.contains(id))
            .collect(Collectors.toSet());

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

            // Solo enviar notificación si es una instalación NUEVA (no estaba asignada antes)
            if (nuevasInstalacionesIds.contains(instalacionId)) {
                try {
                    notificacionService.crearNotificacion(
                        coordinador.getId(),
                        "Nueva instalación asignada",
                        "Se te ha asignado la supervisión de " + instalacion.getNombre() + ". Revisa tus horarios y responsabilidades en la sección de instalaciones.",
                        "asignacion"
                    );
                    System.out.println("Notificación enviada para nueva instalación: " + instalacion.getNombre());
                } catch (Exception e) {
                    System.err.println("Error al enviar notificación de asignación: " + e.getMessage());
                }
            }

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
