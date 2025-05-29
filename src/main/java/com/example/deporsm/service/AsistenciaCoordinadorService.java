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
import java.sql.Time;
import java.sql.Timestamp;
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
        try {
            System.out.println("[DEBUG] Iniciando creación de asistencia con requestDTO: " + requestDTO);

            // Validar datos de entrada
            if (requestDTO.getCoordinadorId() == null) {
                throw new RuntimeException("ID de coordinador es requerido");
            }
            if (requestDTO.getInstalacionId() == null) {
                throw new RuntimeException("ID de instalación es requerido");
            }
            if (requestDTO.getFecha() == null) {
                throw new RuntimeException("Fecha es requerida");
            }

            // Verificar si ya existe una asistencia para este coordinador, instalación, fecha y horario
            java.sql.Time horaProgramadaInicio = convertirStringATime(requestDTO.getHoraProgramadaInicio());
            java.sql.Time horaProgramadaFin = convertirStringATime(requestDTO.getHoraProgramadaFin());
            java.sql.Date fechaSql = convertirStringADate(requestDTO.getFecha());

            List<AsistenciaCoordinador> asistenciasExistentes = asistenciaCoordinadorRepository
                .findByCoordinadorInstalacionFechaYHorario(
                    requestDTO.getCoordinadorId(),
                    requestDTO.getInstalacionId(),
                    fechaSql,
                    horaProgramadaInicio,
                    horaProgramadaFin
                );

            if (!asistenciasExistentes.isEmpty()) {
                System.out.println("[DEBUG] Ya existe una asistencia registrada para este horario");
                throw new RuntimeException("Ya existe un registro de asistencia para este coordinador, instalación, fecha y horario");
            }

            System.out.println("[DEBUG] Buscando coordinador con ID: " + requestDTO.getCoordinadorId());
            Usuario coordinador = usuarioRepository.findById(requestDTO.getCoordinadorId())
                    .orElseThrow(() -> new RuntimeException("No se encontró el coordinador con el ID: " + requestDTO.getCoordinadorId()));
            System.out.println("[DEBUG] Coordinador encontrado: " + coordinador.getNombre());

            System.out.println("[DEBUG] Buscando instalación con ID: " + requestDTO.getInstalacionId());
            Instalacion instalacion = instalacionRepository.findById(requestDTO.getInstalacionId())
                    .orElseThrow(() -> new RuntimeException("No se encontró la instalación con el ID: " + requestDTO.getInstalacionId()));
            System.out.println("[DEBUG] Instalación encontrada: " + instalacion.getNombre());

            System.out.println("[DEBUG] Creando entidad AsistenciaCoordinador");
            AsistenciaCoordinador asistencia = new AsistenciaCoordinador();
            asistencia.setCoordinador(coordinador);
            asistencia.setInstalacion(instalacion);

            // Convertir String a tipos apropiados usando métodos auxiliares
            asistencia.setFecha(convertirStringADate(requestDTO.getFecha()));
            asistencia.setHoraProgramadaInicio(convertirStringATime(requestDTO.getHoraProgramadaInicio()));
            asistencia.setHoraProgramadaFin(convertirStringATime(requestDTO.getHoraProgramadaFin()));
            asistencia.setHoraEntrada(convertirStringATime(requestDTO.getHoraEntrada()));
            asistencia.setHoraSalida(convertirStringATime(requestDTO.getHoraSalida()));
            asistencia.setEstadoEntrada(convertirStringAEstado(requestDTO.getEstadoEntrada()));
            asistencia.setEstadoSalida(convertirStringAEstado(requestDTO.getEstadoSalida()));

            // Si no se proporciona ubicación, usar la ubicación de la instalación
            if (requestDTO.getUbicacion() != null && !requestDTO.getUbicacion().trim().isEmpty()) {
                asistencia.setUbicacion(requestDTO.getUbicacion());
            } else {
                asistencia.setUbicacion(instalacion.getUbicacion());
            }

            asistencia.setNotas(requestDTO.getNotas());

            // Establecer timestamps automáticamente
            Timestamp now = new Timestamp(System.currentTimeMillis());
            asistencia.setCreatedAt(now);
            asistencia.setUpdatedAt(now);

            System.out.println("[DEBUG] Estados de asistencia - Entrada: " + requestDTO.getEstadoEntrada() + ", Salida: " + requestDTO.getEstadoSalida());
            System.out.println("[DEBUG] Guardando asistencia en base de datos");

            AsistenciaCoordinador asistenciaGuardada = asistenciaCoordinadorRepository.save(asistencia);
            System.out.println("[DEBUG] Asistencia guardada con ID: " + asistenciaGuardada.getId());

            AsistenciaCoordinadorDTO dto = convertirADTO(asistenciaGuardada);
            System.out.println("[DEBUG] DTO creado exitosamente");
            return dto;
        } catch (Exception e) {
            System.err.println("[ERROR] Error en crearAsistencia: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
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

        // Actualizar resto de campos usando métodos auxiliares
        if (requestDTO.getFecha() != null) asistencia.setFecha(convertirStringADate(requestDTO.getFecha()));
        if (requestDTO.getHoraProgramadaInicio() != null) asistencia.setHoraProgramadaInicio(convertirStringATime(requestDTO.getHoraProgramadaInicio()));
        if (requestDTO.getHoraProgramadaFin() != null) asistencia.setHoraProgramadaFin(convertirStringATime(requestDTO.getHoraProgramadaFin()));
        if (requestDTO.getHoraEntrada() != null) asistencia.setHoraEntrada(convertirStringATime(requestDTO.getHoraEntrada()));
        if (requestDTO.getEstadoEntrada() != null) asistencia.setEstadoEntrada(convertirStringAEstado(requestDTO.getEstadoEntrada()));
        if (requestDTO.getHoraSalida() != null) asistencia.setHoraSalida(convertirStringATime(requestDTO.getHoraSalida()));
        if (requestDTO.getEstadoSalida() != null) asistencia.setEstadoSalida(convertirStringAEstado(requestDTO.getEstadoSalida()));
        // Si se proporciona ubicación, usarla; si no, mantener la ubicación actual o usar la de la instalación
        if (requestDTO.getUbicacion() != null && !requestDTO.getUbicacion().trim().isEmpty()) {
            asistencia.setUbicacion(requestDTO.getUbicacion());
        } else if (asistencia.getUbicacion() == null || asistencia.getUbicacion().trim().isEmpty()) {
            // Si no tiene ubicación, usar la de la instalación
            asistencia.setUbicacion(asistencia.getInstalacion().getUbicacion());
        }
        if (requestDTO.getNotas() != null) asistencia.setNotas(requestDTO.getNotas());

        // Actualizar timestamp de modificación
        asistencia.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

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

        asistencia.setHoraEntrada(convertirStringATime(requestDTO.getHoraEntrada()));
        asistencia.setEstadoEntrada(convertirStringAEstado(requestDTO.getEstadoEntrada()));
        // Si se proporciona ubicación, usarla; si no, usar la de la instalación
        if (requestDTO.getUbicacion() != null && !requestDTO.getUbicacion().trim().isEmpty()) {
            asistencia.setUbicacion(requestDTO.getUbicacion());
        } else if (asistencia.getUbicacion() == null || asistencia.getUbicacion().trim().isEmpty()) {
            asistencia.setUbicacion(asistencia.getInstalacion().getUbicacion());
        }
        if (requestDTO.getNotas() != null) {
            asistencia.setNotas(requestDTO.getNotas());
        }

        // Actualizar timestamp de modificación
        asistencia.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        AsistenciaCoordinador asistenciaActualizada = asistenciaCoordinadorRepository.save(asistencia);
        return convertirADTO(asistenciaActualizada);
    }

    // Registrar salida del coordinador
    @Transactional
    public AsistenciaCoordinadorDTO registrarSalida(Integer id, AsistenciaCoordinadorRequestDTO requestDTO) {
        System.out.println("[DEBUG] Iniciando registro de salida para asistencia ID: " + id);
        System.out.println("[DEBUG] Datos recibidos: " + requestDTO);

        AsistenciaCoordinador asistencia = asistenciaCoordinadorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la asistencia con el ID: " + id));

        System.out.println("[DEBUG] Asistencia encontrada - ID: " + asistencia.getId());
        System.out.println("[DEBUG] Estado actual - Hora salida: " + asistencia.getHoraSalida() + ", Estado salida: " + asistencia.getEstadoSalida());

        // Actualizar hora y estado de salida
        Time horaSalida = convertirStringATime(requestDTO.getHoraSalida());
        EstadoAsistencia estadoSalida = convertirStringAEstado(requestDTO.getEstadoSalida());

        System.out.println("[DEBUG] Nuevos valores - Hora salida: " + horaSalida + ", Estado salida: " + estadoSalida);

        asistencia.setHoraSalida(horaSalida);
        asistencia.setEstadoSalida(estadoSalida);

        // Si se proporciona ubicación, usarla; si no, usar la de la instalación
        if (requestDTO.getUbicacion() != null && !requestDTO.getUbicacion().trim().isEmpty()) {
            asistencia.setUbicacion(requestDTO.getUbicacion());
            System.out.println("[DEBUG] Ubicación actualizada: " + requestDTO.getUbicacion());
        } else if (asistencia.getUbicacion() == null || asistencia.getUbicacion().trim().isEmpty()) {
            asistencia.setUbicacion(asistencia.getInstalacion().getUbicacion());
            System.out.println("[DEBUG] Usando ubicación de la instalación: " + asistencia.getInstalacion().getUbicacion());
        }

        if (requestDTO.getNotas() != null) {
            String notasActualizadas = asistencia.getNotas() != null ?
                    asistencia.getNotas() + "\n" + requestDTO.getNotas() :
                    requestDTO.getNotas();
            asistencia.setNotas(notasActualizadas);
            System.out.println("[DEBUG] Notas actualizadas: " + notasActualizadas);
        }

        // Actualizar timestamp de modificación
        asistencia.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        System.out.println("[DEBUG] Guardando asistencia actualizada en base de datos");
        AsistenciaCoordinador asistenciaActualizada = asistenciaCoordinadorRepository.save(asistencia);
        System.out.println("[DEBUG] Asistencia guardada - Hora salida: " + asistenciaActualizada.getHoraSalida() + ", Estado salida: " + asistenciaActualizada.getEstadoSalida());

        AsistenciaCoordinadorDTO dto = convertirADTO(asistenciaActualizada);
        System.out.println("[DEBUG] DTO generado - Hora salida: " + dto.getHoraSalida() + ", Estado salida: " + dto.getEstadoSalida());

        return dto;
    }

    // Métodos auxiliares para conversión de tipos
    private Date convertirStringADate(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(fechaStr);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al convertir fecha: " + fechaStr + " - " + e.getMessage());
            return null;
        }
    }

    private Time convertirStringATime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Si no tiene segundos, agregarlos
            if (timeStr.length() == 5) {
                timeStr += ":00";
            }
            return Time.valueOf(timeStr);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al convertir tiempo: " + timeStr + " - " + e.getMessage());
            return null;
        }
    }

    private EstadoAsistencia convertirStringAEstado(String estadoStr) {
        if (estadoStr == null || estadoStr.trim().isEmpty()) {
            return EstadoAsistencia.PENDIENTE;
        }
        try {
            return EstadoAsistencia.fromString(estadoStr);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al convertir estado: " + estadoStr + " - " + e.getMessage());
            return EstadoAsistencia.PENDIENTE;
        }
    }

    // Método auxiliar para convertir entidad a DTO
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
