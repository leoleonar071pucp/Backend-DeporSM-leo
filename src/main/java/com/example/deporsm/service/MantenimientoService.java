package com.example.deporsm.service;

import com.example.deporsm.dto.MantenimientoRequestDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.MantenimientoInstalacion;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.MantenimientoInstalacionRepository;
import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class MantenimientoService {

    @Autowired
    private MantenimientoInstalacionRepository mantenimientoRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private NotificacionService notificacionService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Programa un mantenimiento para una instalación y cancela las reservas afectadas
     * @param requestDTO Datos del mantenimiento a programar
     * @return El mantenimiento programado
     */
    @Transactional
    public MantenimientoInstalacion programarMantenimiento(MantenimientoRequestDTO requestDTO) {
        // Verificar que la instalación existe
        Instalacion instalacion = instalacionRepository.findById(requestDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));

        // Verificar que el usuario que registra existe
        Usuario registrador = usuarioRepository.findById(requestDTO.getRegistradoPorId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Crear una nueva instancia de MantenimientoInstalacion
        MantenimientoInstalacion mantenimiento = new MantenimientoInstalacion();

        // Establecer los datos desde el DTO
        mantenimiento.setInstalacion(instalacion);
        mantenimiento.setRegistradoPor(registrador);
        mantenimiento.setMotivo(requestDTO.getMotivo());
        mantenimiento.setTipo(requestDTO.getTipo());
        mantenimiento.setDescripcion(requestDTO.getDescripcion());
        mantenimiento.setFechaInicio(requestDTO.getFechaInicio());
        mantenimiento.setFechaFin(requestDTO.getFechaFin());
        mantenimiento.setAfectaDisponibilidad(requestDTO.getAfectaDisponibilidad());

        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        mantenimiento.setCreatedAt(now);
        mantenimiento.setUpdatedAt(now);

        // Obtener componentes de fecha y hora para comparaciones más detalladas
        LocalDateTime fechaInicio = mantenimiento.getFechaInicio();
        LocalDateTime fechaFin = mantenimiento.getFechaFin();

        // Comparación detallada para determinar el estado
        boolean esAnteriorAInicio = now.compareTo(fechaInicio) < 0;
        boolean esPosteriorAFin = now.compareTo(fechaFin) >= 0;
        boolean estaEntreFechas = !esAnteriorAInicio && !esPosteriorAFin;

        // Determinar el estado según las comparaciones
        if (esAnteriorAInicio) {
            // Si la fecha/hora actual es anterior a la fecha/hora de inicio
            mantenimiento.setEstado("programado");
        } else if (esPosteriorAFin) {
            // Si la fecha/hora actual es igual o posterior a la fecha/hora de fin
            mantenimiento.setEstado("completado");
        } else if (estaEntreFechas) {
            // Si la fecha/hora actual está entre la fecha/hora de inicio y fin
            mantenimiento.setEstado("en-progreso");
        }

        // Imprimir información de depuración detallada
        System.out.println("=== PROGRAMACIÓN DE MANTENIMIENTO ===");
        System.out.println("Fecha/hora actual: " + now);
        System.out.println("Fecha/hora inicio: " + fechaInicio);
        System.out.println("Fecha/hora fin: " + fechaFin);
        System.out.println("Es anterior a inicio: " + esAnteriorAInicio);
        System.out.println("Es posterior a fin: " + esPosteriorAFin);
        System.out.println("Está entre fechas: " + estaEntreFechas);
        System.out.println("Estado calculado: " + mantenimiento.getEstado());
        System.out.println("====================================");

        // Guardar el mantenimiento
        MantenimientoInstalacion guardado = mantenimientoRepository.save(mantenimiento);

        // Solo cancelar reservas si el mantenimiento afecta la disponibilidad
        if (mantenimiento.getAfectaDisponibilidad() != null && mantenimiento.getAfectaDisponibilidad()) {
            // Buscar reservas afectadas por el mantenimiento
            List<Reserva> reservasAfectadas = buscarReservasAfectadas(
                    instalacion.getId(),
                    mantenimiento.getFechaInicio(),
                    mantenimiento.getFechaFin()
            );

            // Cancelar las reservas afectadas
            for (Reserva reserva : reservasAfectadas) {
                cancelarReservaAutomaticamente(reserva, mantenimiento);
            }
        }

        return guardado;
    }

    /**
     * Programa un mantenimiento para una instalación y cancela las reservas afectadas
     * @param mantenimiento Datos del mantenimiento a programar
     * @return El mantenimiento programado
     */
    @Transactional
    public MantenimientoInstalacion programarMantenimiento(MantenimientoInstalacion mantenimiento) {
        // Verificar que la instalación existe
        Instalacion instalacion = instalacionRepository.findById(mantenimiento.getInstalacion().getId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));

        // Verificar que el usuario que registra existe
        Usuario registrador = usuarioRepository.findById(mantenimiento.getRegistradoPor().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Establecer la instalación y el usuario
        mantenimiento.setInstalacion(instalacion);
        mantenimiento.setRegistradoPor(registrador);

        // Establecer fechas de creación y actualización
        LocalDateTime now = LocalDateTime.now();
        mantenimiento.setCreatedAt(now);
        mantenimiento.setUpdatedAt(now);

        // Obtener componentes de fecha y hora para comparaciones más detalladas
        LocalDateTime fechaInicio = mantenimiento.getFechaInicio();
        LocalDateTime fechaFin = mantenimiento.getFechaFin();

        // Comparación detallada para determinar el estado
        boolean esAnteriorAInicio = now.compareTo(fechaInicio) < 0;
        boolean esPosteriorAFin = now.compareTo(fechaFin) >= 0;
        boolean estaEntreFechas = !esAnteriorAInicio && !esPosteriorAFin;

        // Determinar el estado según las comparaciones
        if (esAnteriorAInicio) {
            // Si la fecha/hora actual es anterior a la fecha/hora de inicio
            mantenimiento.setEstado("programado");
        } else if (esPosteriorAFin) {
            // Si la fecha/hora actual es igual o posterior a la fecha/hora de fin
            mantenimiento.setEstado("completado");
        } else if (estaEntreFechas) {
            // Si la fecha/hora actual está entre la fecha/hora de inicio y fin
            mantenimiento.setEstado("en-progreso");
        }

        // Imprimir información de depuración detallada
        System.out.println("=== PROGRAMACIÓN DE MANTENIMIENTO ===");
        System.out.println("Fecha/hora actual: " + now);
        System.out.println("Fecha/hora inicio: " + fechaInicio);
        System.out.println("Fecha/hora fin: " + fechaFin);
        System.out.println("Es anterior a inicio: " + esAnteriorAInicio);
        System.out.println("Es posterior a fin: " + esPosteriorAFin);
        System.out.println("Está entre fechas: " + estaEntreFechas);
        System.out.println("Estado calculado: " + mantenimiento.getEstado());
        System.out.println("====================================");

        // Guardar el mantenimiento
        MantenimientoInstalacion guardado = mantenimientoRepository.save(mantenimiento);

        // Solo cancelar reservas si el mantenimiento afecta la disponibilidad
        if (mantenimiento.getAfectaDisponibilidad() != null && mantenimiento.getAfectaDisponibilidad()) {
            // Buscar reservas afectadas por el mantenimiento
            List<Reserva> reservasAfectadas = buscarReservasAfectadas(
                    instalacion.getId(),
                    mantenimiento.getFechaInicio(),
                    mantenimiento.getFechaFin()
            );

            // Cancelar las reservas afectadas
            for (Reserva reserva : reservasAfectadas) {
                cancelarReservaAutomaticamente(reserva, mantenimiento);
            }
        }

        return guardado;
    }

    /**
     * Busca las reservas que se verán afectadas por un mantenimiento
     */
    private List<Reserva> buscarReservasAfectadas(Integer instalacionId, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        // Convertir LocalDateTime a Date y Time para la consulta
        LocalDate fechaInicioDate = fechaInicio.toLocalDate();
        LocalDate fechaFinDate = fechaFin.toLocalDate();
        LocalTime horaInicioTime = fechaInicio.toLocalTime();
        LocalTime horaFinTime = fechaFin.toLocalTime();

        // Consulta JPQL para encontrar reservas que se solapan con el período de mantenimiento
        String jpql = "SELECT r FROM Reserva r " +
                "WHERE r.instalacion.id = :instalacionId " +
                "AND r.estado != 'cancelada' " +
                "AND (" +
                // Caso 1: La fecha de la reserva está entre la fecha de inicio y fin del mantenimiento
                "(r.fecha >= :fechaInicio AND r.fecha <= :fechaFin) " +
                // Caso 2: La fecha de la reserva es igual a la fecha de inicio del mantenimiento y la hora de fin de la reserva es después de la hora de inicio del mantenimiento
                "OR (r.fecha = :fechaInicio AND r.horaFin > :horaInicio) " +
                // Caso 3: La fecha de la reserva es igual a la fecha de fin del mantenimiento y la hora de inicio de la reserva es antes de la hora de fin del mantenimiento
                "OR (r.fecha = :fechaFin AND r.horaInicio < :horaFin) " +
                ")";

        return entityManager.createQuery(jpql, Reserva.class)
                .setParameter("instalacionId", instalacionId)
                .setParameter("fechaInicio", Date.valueOf(fechaInicioDate))
                .setParameter("fechaFin", Date.valueOf(fechaFinDate))
                .setParameter("horaInicio", Time.valueOf(horaInicioTime))
                .setParameter("horaFin", Time.valueOf(horaFinTime))
                .getResultList();
    }

    /**
     * Cancela una reserva automáticamente debido a un mantenimiento programado
     */
    private void cancelarReservaAutomaticamente(Reserva reserva, MantenimientoInstalacion mantenimiento) {
        // Cambiar el estado de la reserva a cancelada
        reserva.setEstado("cancelada");

        // Si hay un campo de motivo, establecerlo
        String motivo = "Cancelada automáticamente debido a mantenimiento programado: " + mantenimiento.getMotivo();
        try {
            reserva.setMotivo(motivo);
        } catch (Exception e) {
            // Si el campo no existe, ignorar
            System.out.println("No se pudo establecer el motivo de cancelación: " + e.getMessage());
        }

        // Guardar la reserva
        reservaRepository.save(reserva);

        // Enviar notificación al usuario
        String mensaje = "Tu reserva para " + reserva.getInstalacion().getNombre() +
                " el día " + reserva.getFecha() + " de " + reserva.getHoraInicio() +
                " a " + reserva.getHoraFin() + " ha sido cancelada debido a un mantenimiento programado.";

        notificacionService.crearNotificacion(
                reserva.getUsuario().getId(),
                "Reserva cancelada por mantenimiento",
                mensaje,
                "reserva"
        );
    }

    /**
     * Actualiza el estado de un mantenimiento según sus fechas y horas
     * @param mantenimiento El mantenimiento a actualizar
     * @return El mantenimiento con el estado actualizado
     */
    @Transactional
    public MantenimientoInstalacion actualizarEstadoMantenimiento(MantenimientoInstalacion mantenimiento) {
        // Si el mantenimiento está cancelado, no cambiar su estado
        if ("cancelado".equals(mantenimiento.getEstado())) {
            return mantenimiento;
        }

        LocalDateTime now = LocalDateTime.now();
        String estadoAnterior = mantenimiento.getEstado();
        String nuevoEstado = estadoAnterior;

        // Obtener componentes de fecha y hora para comparaciones más detalladas
        LocalDateTime fechaInicio = mantenimiento.getFechaInicio();
        LocalDateTime fechaFin = mantenimiento.getFechaFin();

        // Comparación detallada para determinar el estado
        boolean esAnteriorAInicio = now.compareTo(fechaInicio) < 0;
        boolean esPosteriorAFin = now.compareTo(fechaFin) >= 0;
        boolean estaEntreFechas = !esAnteriorAInicio && !esPosteriorAFin;

        // Determinar el estado según las comparaciones
        if (esAnteriorAInicio) {
            // Si la fecha/hora actual es anterior a la fecha/hora de inicio
            nuevoEstado = "programado";
        } else if (esPosteriorAFin) {
            // Si la fecha/hora actual es igual o posterior a la fecha/hora de fin
            nuevoEstado = "completado";
        } else if (estaEntreFechas) {
            // Si la fecha/hora actual está entre la fecha/hora de inicio y fin
            nuevoEstado = "en-progreso";
        }

        // Imprimir información de depuración detallada
        System.out.println("=== ACTUALIZACIÓN DE ESTADO DE MANTENIMIENTO ===");
        System.out.println("ID: " + mantenimiento.getId());
        System.out.println("Fecha/hora actual: " + now);
        System.out.println("Fecha/hora inicio: " + fechaInicio);
        System.out.println("Fecha/hora fin: " + fechaFin);
        System.out.println("Es anterior a inicio: " + esAnteriorAInicio);
        System.out.println("Es posterior a fin: " + esPosteriorAFin);
        System.out.println("Está entre fechas: " + estaEntreFechas);
        System.out.println("Estado anterior: " + estadoAnterior);
        System.out.println("Nuevo estado: " + nuevoEstado);
        System.out.println("==============================================");

        // Siempre guardar el estado actualizado para asegurar que se aplique
        if (!nuevoEstado.equals(estadoAnterior)) {
            mantenimiento.setEstado(nuevoEstado);
            mantenimiento.setUpdatedAt(LocalDateTime.now());
            MantenimientoInstalacion actualizado = mantenimientoRepository.save(mantenimiento);
            System.out.println("Estado actualizado y guardado en la base de datos.");
            return actualizado;
        }

        return mantenimiento;
    }

    /**
     * Tarea programada para actualizar automáticamente el estado de todos los mantenimientos
     * Se ejecuta cada 1 minuto
     */
    @Scheduled(fixedRate = 60000) // 1 minuto = 60,000 ms
    @Transactional
    public void actualizarEstadosMantenimientos() {
        try {
            System.out.println("\n=== INICIANDO ACTUALIZACIÓN AUTOMÁTICA DE ESTADOS ===");
            System.out.println("Fecha/hora actual: " + LocalDateTime.now());

            // Obtener todos los mantenimientos
            List<MantenimientoInstalacion> mantenimientos = mantenimientoRepository.findAll();
            System.out.println("Total de mantenimientos encontrados: " + mantenimientos.size());

            // Actualizar todos los mantenimientos que no estén en estados finales
            int actualizados = 0;
            for (MantenimientoInstalacion m : mantenimientos) {
                if (!"cancelado".equals(m.getEstado()) && !"completado".equals(m.getEstado())) {
                    String estadoAnterior = m.getEstado();
                    actualizarEstadoMantenimiento(m);
                    if (!estadoAnterior.equals(m.getEstado())) {
                        actualizados++;
                    }
                }
            }

            System.out.println("Mantenimientos actualizados: " + actualizados);
            System.out.println("Actualización automática completada: " + LocalDateTime.now());
            System.out.println("=== FIN DE ACTUALIZACIÓN AUTOMÁTICA ===\n");
        } catch (Exception e) {
            System.err.println("ERROR AL ACTUALIZAR ESTADOS DE MANTENIMIENTOS: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
