package com.example.deporsm.service;

import com.example.deporsm.dto.AsistenciaCoordinadorDTO;
import com.example.deporsm.dto.AsistenciaCoordinadorRequestDTO;
import com.example.deporsm.dto.AsistenciaCoordinadorResumenDTO;
import com.example.deporsm.model.AsistenciaCoordinador;
import com.example.deporsm.model.AsistenciaCoordinador.EstadoAsistencia;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.AsistenciaCoordinadorRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AsistenciaCoordinadorService {

    @Autowired
    private AsistenciaCoordinadorRepository asistenciaCoordinadorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    // Obtener todas las asistencias
    public List<AsistenciaCoordinadorDTO> obtenerTodasAsistencias() {
        List<AsistenciaCoordinador> asistencias = asistenciaCoordinadorRepository.findAll();
        return asistencias.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    // Obtener asistencia por id
    public AsistenciaCoordinadorDTO obtenerAsistenciaPorId(Integer id) {
        AsistenciaCoordinador asistencia = asistenciaCoordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la asistencia con el ID: " + id));
        return convertirADTO(asistencia);
    }

    // Obtener asistencias por coordinador
    public List<AsistenciaCoordinadorDTO> obtenerAsistenciasPorCoordinador(Integer coordinadorId) {
        Usuario coordinador = usuarioRepository.findById(coordinadorId)
                .orElseThrow(() -> new RuntimeException("No se encontró el coordinador con el ID: " + coordinadorId));
        List<AsistenciaCoordinador> asistencias = asistenciaCoordinadorRepository.findByCoordinador(coordinador);
        return asistencias.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    // Obtener asistencias por instalación
    public List<AsistenciaCoordinadorDTO> obtenerAsistenciasPorInstalacion(Integer instalacionId) {
        Instalacion instalacion = instalacionRepository.findById(instalacionId)
                .orElseThrow(() -> new RuntimeException("No se encontró la instalación con el ID: " + instalacionId));
        List<AsistenciaCoordinador> asistencias = asistenciaCoordinadorRepository.findByInstalacion(instalacion);
        return asistencias.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    // Obtener asistencias por fecha
    public List<AsistenciaCoordinadorDTO> obtenerAsistenciasPorFecha(Date fecha) {
        List<AsistenciaCoordinador> asistencias = asistenciaCoordinadorRepository.findByFecha(fecha);
        return asistencias.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    // Obtener asistencias por coordinador y rango de fechas
    public List<AsistenciaCoordinadorDTO> obtenerAsistenciasPorCoordinadorYRangoFechas(
            Integer coordinadorId, Date fechaInicio, Date fechaFin) {
        List<AsistenciaCoordinador> asistencias = asistenciaCoordinadorRepository
                .findByCoordinadorIdAndFechaBetween(coordinadorId, fechaInicio, fechaFin);
        return asistencias.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    // Obtener resumen de asistencias por coordinador
    public AsistenciaCoordinadorResumenDTO obtenerResumenAsistenciasCoordinador(Integer coordinadorId) {
        Usuario coordinador = usuarioRepository.findById(coordinadorId)
                .orElseThrow(() -> new RuntimeException("No se encontró el coordinador con el ID: " + coordinadorId));
        
        List<AsistenciaCoordinador> asistencias = asistenciaCoordinadorRepository.findByCoordinador(coordinador);
        List<AsistenciaCoordinadorDTO> asistenciasDTO = asistencias.stream().map(this::convertirADTO).collect(Collectors.toList());
        
        // Contar por estado
        long atiempo = asistencias.stream().filter(a -> EstadoAsistencia.A_TIEMPO.equals(a.getEstadoEntrada())).count();
        long tarde = asistencias.stream().filter(a -> EstadoAsistencia.TARDE.equals(a.getEstadoEntrada())).count();
        long noAsistio = asistencias.stream().filter(a -> EstadoAsistencia.NO_ASISTIO.equals(a.getEstadoEntrada())).count();
        long pendiente = asistencias.stream().filter(a -> EstadoAsistencia.PENDIENTE.equals(a.getEstadoSalida())).count();
        
        // Agrupar asistencias por fecha
        Map<String, List<AsistenciaCoordinadorDTO>> asistenciasPorFecha = new HashMap<>();
        for (AsistenciaCoordinadorDTO dto : asistenciasDTO) {
            String fechaStr = dto.getFecha().toString();
            if (!asistenciasPorFecha.containsKey(fechaStr)) {
                asistenciasPorFecha.put(fechaStr, new ArrayList<>());
            }
            asistenciasPorFecha.get(fechaStr).add(dto);
        }
          AsistenciaCoordinadorResumenDTO resumen = new AsistenciaCoordinadorResumenDTO();
        resumen.setCoordinadorId(coordinadorId);
        resumen.setNombreCoordinador(coordinador.getNombre() + " " + coordinador.getApellidos());
        resumen.setTotalAsistencias(asistencias.size());
        resumen.setAtiempo(atiempo);
        resumen.setTarde(tarde);
        resumen.setNoAsistio(noAsistio);
        resumen.setPendiente(pendiente);
        resumen.setAsistenciasPorFecha(asistenciasPorFecha);
        
        return resumen;
    }

    // Crear nueva asistencia
    @Transactional
    public AsistenciaCoordinadorDTO crearAsistencia(AsistenciaCoordinadorRequestDTO requestDTO) {
        Usuario coordinador = usuarioRepository.findById(requestDTO.getCoordinadorId())
                .orElseThrow(() -> new RuntimeException("No se encontró el coordinador con el ID: " + requestDTO.getCoordinadorId()));
        
        Instalacion instalacion = instalacionRepository.findById(requestDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("No se encontró la instalación con el ID: " + requestDTO.getInstalacionId()));
        
        AsistenciaCoordinador asistencia = new AsistenciaCoordinador();
        asistencia.setCoordinador(coordinador);
        asistencia.setInstalacion(instalacion);
        asistencia.setFecha(requestDTO.getFecha());
        asistencia.setHoraProgramadaInicio(requestDTO.getHoraProgramadaInicio());
        asistencia.setHoraProgramadaFin(requestDTO.getHoraProgramadaFin());
        asistencia.setHoraEntrada(requestDTO.getHoraEntrada());
        asistencia.setEstadoEntrada(requestDTO.getEstadoEntrada());
        asistencia.setHoraSalida(requestDTO.getHoraSalida());
        asistencia.setEstadoSalida(requestDTO.getEstadoSalida());
        asistencia.setUbicacion(requestDTO.getUbicacion());
        asistencia.setNotas(requestDTO.getNotas());
        
        AsistenciaCoordinador asistenciaGuardada = asistenciaCoordinadorRepository.save(asistencia);
        return convertirADTO(asistenciaGuardada);
    }

    // Actualizar asistencia existente
    @Transactional
    public AsistenciaCoordinadorDTO actualizarAsistencia(Integer id, AsistenciaCoordinadorRequestDTO requestDTO) {
        AsistenciaCoordinador asistencia = asistenciaCoordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la asistencia con el ID: " + id));
        
        // Solo actualizar si los IDs existen
        if (requestDTO.getCoordinadorId() != null) {
            Usuario coordinador = usuarioRepository.findById(requestDTO.getCoordinadorId())
                    .orElseThrow(() -> new RuntimeException("No se encontró el coordinador con el ID: " + requestDTO.getCoordinadorId()));
            asistencia.setCoordinador(coordinador);
        }
        
        if (requestDTO.getInstalacionId() != null) {
            Instalacion instalacion = instalacionRepository.findById(requestDTO.getInstalacionId())
                    .orElseThrow(() -> new RuntimeException("No se encontró la instalación con el ID: " + requestDTO.getInstalacionId()));
            asistencia.setInstalacion(instalacion);
        }
        
        // Actualizar resto de campos
        if (requestDTO.getFecha() != null) asistencia.setFecha(requestDTO.getFecha());
        if (requestDTO.getHoraProgramadaInicio() != null) asistencia.setHoraProgramadaInicio(requestDTO.getHoraProgramadaInicio());
        if (requestDTO.getHoraProgramadaFin() != null) asistencia.setHoraProgramadaFin(requestDTO.getHoraProgramadaFin());
        if (requestDTO.getHoraEntrada() != null) asistencia.setHoraEntrada(requestDTO.getHoraEntrada());
        if (requestDTO.getEstadoEntrada() != null) asistencia.setEstadoEntrada(requestDTO.getEstadoEntrada());
        if (requestDTO.getHoraSalida() != null) asistencia.setHoraSalida(requestDTO.getHoraSalida());
        if (requestDTO.getEstadoSalida() != null) asistencia.setEstadoSalida(requestDTO.getEstadoSalida());
        if (requestDTO.getUbicacion() != null) asistencia.setUbicacion(requestDTO.getUbicacion());
        if (requestDTO.getNotas() != null) asistencia.setNotas(requestDTO.getNotas());
        
        AsistenciaCoordinador asistenciaActualizada = asistenciaCoordinadorRepository.save(asistencia);
        return convertirADTO(asistenciaActualizada);
    }

    // Eliminar asistencia
    @Transactional
    public void eliminarAsistencia(Integer id) {
        if (!asistenciaCoordinadorRepository.existsById(id)) {
            throw new RuntimeException("No se encontró la asistencia con el ID: " + id);
        }
        asistenciaCoordinadorRepository.deleteById(id);
    }

    // Registrar entrada del coordinador
    @Transactional
    public AsistenciaCoordinadorDTO registrarEntrada(Integer id, AsistenciaCoordinadorRequestDTO requestDTO) {
        AsistenciaCoordinador asistencia = asistenciaCoordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la asistencia con el ID: " + id));
        
        asistencia.setHoraEntrada(requestDTO.getHoraEntrada());
        asistencia.setEstadoEntrada(requestDTO.getEstadoEntrada());
        if (requestDTO.getUbicacion() != null) {
            asistencia.setUbicacion(requestDTO.getUbicacion());
        }
        if (requestDTO.getNotas() != null) {
            asistencia.setNotas(requestDTO.getNotas());
        }
        
        AsistenciaCoordinador asistenciaActualizada = asistenciaCoordinadorRepository.save(asistencia);
        return convertirADTO(asistenciaActualizada);
    }

    // Registrar salida del coordinador
    @Transactional
    public AsistenciaCoordinadorDTO registrarSalida(Integer id, AsistenciaCoordinadorRequestDTO requestDTO) {
        AsistenciaCoordinador asistencia = asistenciaCoordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la asistencia con el ID: " + id));
        
        asistencia.setHoraSalida(requestDTO.getHoraSalida());
        asistencia.setEstadoSalida(requestDTO.getEstadoSalida());
        if (requestDTO.getUbicacion() != null) {
            asistencia.setUbicacion(requestDTO.getUbicacion());
        }
        if (requestDTO.getNotas() != null) {
            String notasActualizadas = asistencia.getNotas() != null ? 
                    asistencia.getNotas() + "\n" + requestDTO.getNotas() : 
                    requestDTO.getNotas();
            asistencia.setNotas(notasActualizadas);
        }
        
        AsistenciaCoordinador asistenciaActualizada = asistenciaCoordinadorRepository.save(asistencia);
        return convertirADTO(asistenciaActualizada);
    }    // Método auxiliar para convertir entidad a DTO
    private AsistenciaCoordinadorDTO convertirADTO(AsistenciaCoordinador asistencia) {
        AsistenciaCoordinadorDTO dto = new AsistenciaCoordinadorDTO();
        dto.setId(asistencia.getId());
        dto.setCoordinadorId(asistencia.getCoordinador().getId());
        dto.setNombreCoordinador(asistencia.getCoordinador().getNombre() + " " + asistencia.getCoordinador().getApellidos());
        dto.setInstalacionId(asistencia.getInstalacion().getId());
        dto.setNombreInstalacion(asistencia.getInstalacion().getNombre());
        dto.setFecha(asistencia.getFecha());
        dto.setHoraProgramadaInicio(asistencia.getHoraProgramadaInicio());
        dto.setHoraProgramadaFin(asistencia.getHoraProgramadaFin());
        dto.setHoraEntrada(asistencia.getHoraEntrada());
        dto.setEstadoEntrada(asistencia.getEstadoEntrada());
        dto.setHoraSalida(asistencia.getHoraSalida());
        dto.setEstadoSalida(asistencia.getEstadoSalida());
        dto.setUbicacion(asistencia.getUbicacion());
        dto.setNotas(asistencia.getNotas());
        return dto;
    }
}
