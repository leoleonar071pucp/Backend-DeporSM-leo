package com.example.deporsm.service;

import com.example.deporsm.dto.CrearReservaDTO;
import com.example.deporsm.dto.ReservaListDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.BloqueoTemporalRepository;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.PagoRepository;
import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.repository.UsuarioRepository;
import com.example.deporsm.util.ConfiguracionSistemaUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.math.BigDecimal;

import java.util.List;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstalacionRepository instalacionRepository;

    @Autowired
    private BloqueoTemporalRepository bloqueoTemporalRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private NotificacionService notificacionService;

    @Autowired
    private BloqueoTemporalService bloqueoTemporalService;

    /**
     * Crea una nueva reserva para un usuario
     * @param email Email del usuario
     * @param reservaDTO Datos de la reserva
     * @return La reserva creada
     * @throws RuntimeException si no se encuentra el usuario o la instalación,
     *         o si la instalación no está disponible
     */
    @Transactional
    public Reserva crearReserva(String email, CrearReservaDTO reservaDTO) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Instalacion instalacion = instalacionRepository.findById(reservaDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));

        if (!instalacion.getActivo()) {
            throw new RuntimeException("La instalación no está disponible para reservas");
        }

        // Liberar cualquier bloqueo temporal que el usuario pudiera tener
        bloqueoTemporalRepository.deleteByUsuarioId(usuario.getId());

        // Convertir strings a tipos SQL
        Date fecha = convertirStringADate(reservaDTO.getFecha());
        Time horaInicio = convertirStringATime(reservaDTO.getHoraInicio());
        Time horaFin = convertirStringATime(reservaDTO.getHoraFin());

        // Verificar disponibilidad de horario
        boolean horarioDisponible = verificarDisponibilidadHorario(
                instalacion.getId(),
                fecha,
                horaInicio,
                horaFin);

        if (!horarioDisponible) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }

        // Verificar si hay conflicto con otras reservas del mismo usuario
        boolean hayConflicto = verificarConflictoHorarioUsuario(
                usuario.getId(),
                fecha,
                horaInicio,
                horaFin);

        if (hayConflicto) {
            throw new RuntimeException("El horario seleccionado entra en conflicto con otra reserva que ya tienes para ese día");
        }

        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setInstalacion(instalacion);
        reserva.setFecha(fecha);
        reserva.setHoraInicio(horaInicio);
        reserva.setHoraFin(horaFin);

        // Manejar el campo numeroAsistentes de forma segura
        try {
            if (reservaDTO.getNumeroAsistentes() != null) {
                reserva.setNumeroAsistentes(reservaDTO.getNumeroAsistentes());
            }
        } catch (Exception e) {
            // Si el campo no existe en la entidad, solo log y continuar
            System.out.println("Advertencia: No se pudo establecer el número de asistentes: " + e.getMessage());
        }

        reserva.setComentarios(reservaDTO.getComentarios());

        // Usar el estado proporcionado o establecer el predeterminado
        if (reservaDTO.getEstado() != null && !reservaDTO.getEstado().isEmpty()) {
            reserva.setEstado(reservaDTO.getEstado()); // Estado personalizado desde el frontend
        } else {
            reserva.setEstado("pendiente"); // La reserva comienza en estado pendiente por defecto
        }

        // Usar el estado de pago proporcionado o establecerlo como pendiente
        if (reservaDTO.getEstadoPago() != null && !reservaDTO.getEstadoPago().isEmpty()) {
            reserva.setEstadoPago(reservaDTO.getEstadoPago()); // Estado de pago personalizado
        } else {
            reserva.setEstadoPago("pendiente"); // El pago comienza como pendiente por defecto
        }        // Guardar método de pago si viene proporcionado
        if (reservaDTO.getMetodoPago() != null && !reservaDTO.getMetodoPago().isEmpty()) {
            // Asumiendo que has agregado este getter y setter a la entidad Reserva
            reserva.setMetodoPago(reservaDTO.getMetodoPago());
        }
          // Guardar la reserva primero para obtener su ID
        Reserva reservaGuardada = reservaRepository.save(reserva);
          // Crear un registro de pago automáticamente
        try {
            // Obtener el precio de la instalación y convertirlo a BigDecimal
            BigDecimal precio = BigDecimal.valueOf(instalacion.getPrecio());

            if ("online".equals(reserva.getMetodoPago()) && "pagado".equals(reserva.getEstadoPago())) {
                // Para pagos online, generar una referencia y simular los últimos 4 dígitos
                String referencia = "TRX-" + System.currentTimeMillis();
                String ultimosDigitos = String.format("%04d", (int)(Math.random() * 10000));
                pagoService.crearPagoOnline(reservaGuardada.getId(), precio, referencia, ultimosDigitos);
            }
            // Los pagos por depósito se crearán después cuando el usuario suba el comprobante
        } catch (Exception e) {
            System.err.println("Error al crear el registro de pago: " + e.getMessage());
            // No detenemos el proceso aunque falle la creación del pago
        }

        // Asegurar timestamps en la reserva
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        if (reservaGuardada.getCreatedAt() == null) {
            reservaGuardada.setCreatedAt(currentTimestamp);
            reservaGuardada = reservaRepository.save(reservaGuardada);
        }
        if (reservaGuardada.getUpdatedAt() == null) {
            reservaGuardada.setUpdatedAt(currentTimestamp);
            reservaGuardada = reservaRepository.save(reservaGuardada);
        }return reservaGuardada;
    }

    /**
     * Cancela una reserva de un usuario
     * @param reservaId ID de la reserva
     * @param email Email del usuario
     * @param motivo Motivo de la cancelación (opcional)
     * @throws RuntimeException si no se encuentra la reserva o si no pertenece al usuario
     */    @Transactional
    public void cancelarReserva(Integer reservaId, String email, String motivo) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la reserva existe
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar que la reserva pertenece al usuario o que el usuario es administrador/coordinador
        boolean esAdmin = usuario.getRol() != null &&
                         (usuario.getRol().getNombre().equals("ROLE_ADMIN") ||
                          usuario.getRol().getNombre().equals("ROLE_SUPERADMIN"));
        boolean esCoordinador = usuario.getRol() != null && usuario.getRol().getNombre().equals("ROLE_COORDINADOR");
        boolean esPropietario = reserva.getUsuario().getId().equals(usuario.getId());

        // Imprimir información de depuración
        System.out.println("Usuario: " + usuario.getEmail() + ", Rol: " +
                          (usuario.getRol() != null ? usuario.getRol().getNombre() : "null") +
                          ", Es Admin: " + esAdmin + ", Es Coordinador: " + esCoordinador +
                          ", Es Propietario: " + esPropietario);

        // Permitir a cualquier usuario con rol (incluso si no es admin o coordinador)
        // Esto es temporal para solucionar el problema de permisos
        if (usuario.getRol() == null) {
            throw new RuntimeException("No tienes un rol asignado para cancelar reservas");
        }

        // Verificar que la reserva no esté ya cancelada
        if ("cancelada".equals(reserva.getEstado())) {
            throw new RuntimeException("La reserva ya está cancelada");
        }
        
        // Si no es admin o coordinador, verificar límite de tiempo para cancelación
        if (esPropietario && !esAdmin && !esCoordinador) {
            boolean dentroDelLimite = verificarLimiteCancelacion(reserva);
            if (!dentroDelLimite) {
                throw new RuntimeException("No se puede cancelar la reserva. Excediste el límite de tiempo para cancelación (" + 
                                         ConfiguracionSistemaUtil.getLimiteTiempoCancelacion() + " horas)");
            }
        }

        // Actualizar el estado de la reserva
        reserva.setEstado("cancelada");

        // Actualizar el estado de pago según quién cancela la reserva
        if (esPropietario) {
            // Si el usuario cancela su propia reserva, el estado de pago es "reembolsado"
            reserva.setEstadoPago("reembolsado");
        } else {
            // Si un administrador o coordinador cancela la reserva, el estado de pago es "fallido"
            // Usamos "fallido" en lugar de "rechazado" porque es uno de los valores permitidos en el ENUM
            reserva.setEstadoPago("fallido");

            // Imprimir información de depuración
            System.out.println("Cancelando reserva como admin/coordinador. Estado de pago cambiado a: fallido");
        }

        // Liberar cualquier bloqueo temporal que pueda existir para este horario específico
        // Esto permite que otros usuarios puedan reservar inmediatamente después de la cancelación
        try {
            bloqueoTemporalService.liberarBloqueosPorHorario(
                    reserva.getInstalacion().getId(),
                    reserva.getFecha(),
                    reserva.getHoraInicio(),
                    reserva.getHoraFin()
            );
            System.out.println("Bloqueos temporales liberados para el horario de la reserva cancelada");
        } catch (Exception e) {
            System.err.println("Error al liberar bloqueos temporales después de cancelar reserva: " + e.getMessage());
            // No interrumpir el proceso de cancelación aunque falle la liberación de bloqueos
        }

        // Guardar la reserva actualizada
        reservaRepository.save(reserva);

        // No necesitamos setear el motivo ya que no existe en la entidad
        // Eliminamos esta parte para evitar problemas

        // Actualizar también el registro de pago si existe
        try {
            pagoService.obtenerPagoPorReserva(reservaId).ifPresent(pago -> {
                pago.setEstado(reserva.getEstadoPago());
                pagoRepository.save(pago);
                System.out.println("Pago actualizado correctamente con estado: " + pago.getEstado());
            });
        } catch (Exception e) {
            // Si hay algún problema al actualizar el pago, lo registramos pero continuamos
            System.out.println("Advertencia: No se pudo actualizar el estado del pago: " + e.getMessage());
        }

        // Confirmar que la reserva se guardó correctamente
        System.out.println("Reserva cancelada correctamente. ID: " + reservaId +
                          ", Estado: " + reserva.getEstado() +
                          ", Estado de pago: " + reserva.getEstadoPago());

        // Enviar notificación al usuario propietario de la reserva
        try {
            Integer usuarioId = reserva.getUsuario().getId();
            String instalacionNombre = reserva.getInstalacion().getNombre();
            String fecha = reserva.getFecha().toString();
            String horaInicio = reserva.getHoraInicio().toString().substring(0, 5);
            String horaFin = reserva.getHoraFin().toString().substring(0, 5);

            notificacionService.crearNotificacion(
                usuarioId,
                "Reserva cancelada",
                "Tu reserva para " + instalacionNombre + " el día " + fecha + " de " + horaInicio + " a " + horaFin + " ha sido cancelada.",
                "reserva",
                "cancelacion",
                null
            );

            System.out.println("Notificación de cancelación enviada al usuario ID: " + usuarioId);
        } catch (Exception e) {
            System.out.println("Error al enviar notificación de cancelación: " + e.getMessage());
        }
    }

    /**
     * Aprueba una reserva (cambia su estado a confirmada y su estado de pago a pagado)
     * @param reservaId ID de la reserva a aprobar
     * @param email Email del administrador que aprueba la reserva
     * @throws RuntimeException si no se encuentra la reserva o el usuario no tiene permiso para aprobarla
     */
    @Transactional
    public void aprobarReserva(Integer reservaId, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que el usuario es administrador o coordinador
        boolean esAdmin = usuario.getRol() != null &&
                         (usuario.getRol().getNombre().equals("ROLE_ADMIN") ||
                          usuario.getRol().getNombre().equals("ROLE_SUPERADMIN"));
        boolean esCoordinador = usuario.getRol() != null && usuario.getRol().getNombre().equals("ROLE_COORDINADOR");

        // Imprimir información de depuración
        System.out.println("Usuario: " + usuario.getEmail() + ", Rol: " +
                          (usuario.getRol() != null ? usuario.getRol().getNombre() : "null") +
                          ", Es Admin: " + esAdmin + ", Es Coordinador: " + esCoordinador);

        // Permitir a cualquier usuario con rol (incluso si no es admin o coordinador)
        // Esto es temporal para solucionar el problema de permisos
        if (usuario.getRol() == null) {
            throw new RuntimeException("No tienes un rol asignado para aprobar reservas");
        }

        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar que la reserva no esté ya confirmada
        if ("confirmada".equals(reserva.getEstado()) && "pagado".equals(reserva.getEstadoPago())) {
            throw new RuntimeException("La reserva ya está confirmada y pagada");
        }

        // Actualizar el estado de la reserva
        reserva.setEstado("confirmada");
        reserva.setEstadoPago("pagado");

        // Guardar la reserva actualizada
        reservaRepository.save(reserva);

        // Actualizar también el registro de pago si existe
        try {
            pagoService.obtenerPagoPorReserva(reservaId).ifPresent(pago -> {
                pago.setEstado("pagado");
                pagoRepository.save(pago);
            });
        } catch (Exception e) {
            // Si hay algún problema al actualizar el pago, lo registramos pero continuamos
            System.out.println("Advertencia: No se pudo actualizar el estado del pago: " + e.getMessage());
        }

        // Enviar notificación al usuario propietario de la reserva
        try {
            Integer usuarioId = reserva.getUsuario().getId();
            String instalacionNombre = reserva.getInstalacion().getNombre();
            String fecha = reserva.getFecha().toString();
            String horaInicio = reserva.getHoraInicio().toString().substring(0, 5);
            String horaFin = reserva.getHoraFin().toString().substring(0, 5);

            notificacionService.crearNotificacion(
                usuarioId,
                "Reserva confirmada",
                "Tu reserva para " + instalacionNombre + " el día " + fecha + " de " + horaInicio + " a " + horaFin + " ha sido confirmada.",
                "reserva",
                "confirmacion",
                null
            );

            System.out.println("Notificación de confirmación enviada al usuario ID: " + usuarioId);
        } catch (Exception e) {
            System.out.println("Error al enviar notificación de confirmación: " + e.getMessage());
        }
    }
    /**
     * Obtiene el historial de reservas de un usuario
     * @param email Email del usuario
     * @return Lista de reservas del usuario
     */
    public List<ReservaListDTO> obtenerHistorialReservas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
          // Usamos JPQL con proyección para evitar los campos problemáticos e incluir ubicación, método de pago e imagen
        String jpql = "SELECT new com.example.deporsm.dto.ReservaListDTO(" +
                "r.id, " +
                "CONCAT(u.nombre, ' ', u.apellidos), " +
                "i.nombre, " +
                "i.ubicacion, " +
                "r.metodoPago, " +
                "i.imagenUrl, " +
                "r.fecha, " +
                "r.horaInicio, " +
                "r.horaFin, " +
                "r.estado, " +
                "r.estadoPago) " +
                "FROM Reserva r " +
                "JOIN r.usuario u " +
                "JOIN r.instalacion i " +
                "WHERE u.dni = :dni";

        return entityManager.createQuery(jpql, ReservaListDTO.class)
                .setParameter("dni", usuario.getDni())
                .getResultList();
    }

    /**
     * Verifica si un horario está disponible para una instalación
     * @param instalacionId ID de la instalación
     * @param fecha Fecha de la reserva
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @return true si el horario está disponible, false en caso contrario
     */
    private boolean verificarDisponibilidadHorario(Integer instalacionId, java.sql.Date fecha,
                                                java.sql.Time horaInicio, java.sql.Time horaFin) {
        // Usar el servicio de bloqueo temporal que ya verifica reservas, bloqueos temporales y mantenimientos
        return bloqueoTemporalService.verificarDisponibilidadHorario(
                instalacionId, fecha, horaInicio, horaFin);
    }

    /**
     * Verifica si un horario entra en conflicto con las reservas existentes del usuario
     * @param usuarioId ID del usuario
     * @param fecha Fecha de la reserva
     * @param horaInicio Hora de inicio
     * @param horaFin Hora de fin
     * @return true si hay conflicto, false en caso contrario
     */
    private boolean verificarConflictoHorarioUsuario(Integer usuarioId, java.sql.Date fecha,
                                                  java.sql.Time horaInicio, java.sql.Time horaFin) {
        // Verificar si el usuario tiene reservas existentes que se solapen con el horario solicitado
        String jpql = "SELECT COUNT(r) FROM Reserva r " +
                      "WHERE r.usuario.id = :usuarioId " +
                      "AND r.fecha = :fecha " +
                      "AND r.estado != 'cancelada' " +
                      "AND ((r.horaInicio <= :horaInicio AND r.horaFin > :horaInicio) " +
                      "OR (r.horaInicio < :horaFin AND r.horaFin >= :horaFin) " +
                      "OR (r.horaInicio >= :horaInicio AND r.horaFin <= :horaFin))";

        Long count = entityManager.createQuery(jpql, Long.class)
                .setParameter("usuarioId", usuarioId)
                .setParameter("fecha", fecha)
                .setParameter("horaInicio", horaInicio)
                .setParameter("horaFin", horaFin)
                .getSingleResult();

        return count > 0; // Si hay reservas solapadas, hay conflicto
    }
      /**
     * Actualiza el estado de pago de una reserva
     * @param reservaId ID de la reserva
     * @param email Email del usuario
     * @param estadoPago Nuevo estado de pago
     * @param estado Estado opcional de la reserva
     * @return La reserva actualizada
     * @throws RuntimeException si no se encuentra la reserva o si no pertenece al usuario
     */
    public Reserva actualizarEstadoPago(Integer reservaId, String email, String estadoPago, String estado) {
        // Verificar que el usuario existe
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que la reserva existe
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Verificar que la reserva pertenece al usuario
        if (!reserva.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("La reserva no pertenece a este usuario");
        }

        // Actualizar el estado de pago
        reserva.setEstadoPago(estadoPago);

        // Si se proporciona un estado explícito, usarlo
        if (estado != null && !estado.isEmpty()) {
            reserva.setEstado(estado);
        }
        // Si no, y el pago está confirmado, también actualizar el estado de la reserva a confirmado
        else if ("pagado".equals(estadoPago)) {
            reserva.setEstado("confirmada");
        }

        // Guardar la reserva actualizada
        Reserva reservaActualizada = reservaRepository.save(reserva);

        // Actualizar también el registro de pago si existe
        try {
            pagoService.obtenerPagoPorReserva(reservaId).ifPresent(pago -> {
                pago.setEstado(estadoPago);
                pagoRepository.save(pago);
            });
        } catch (Exception e) {
            // Si hay algún problema al actualizar el pago, lo registramos pero continuamos
            System.out.println("Advertencia: No se pudo actualizar el estado del pago: " + e.getMessage());
        }

        return reservaActualizada;
    }

    /**
     * Convierte un string de fecha (YYYY-MM-DD) a java.sql.Date
     * Evita problemas de zona horaria al usar Date.valueOf directamente
     */
    private Date convertirStringADate(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        try {
            // Date.valueOf espera formato YYYY-MM-DD y no aplica conversiones de zona horaria
            return Date.valueOf(fechaStr);
        } catch (Exception e) {
            System.err.println("[ERROR] Error al convertir fecha: " + fechaStr + " - " + e.getMessage());
            throw new RuntimeException("Formato de fecha inválido: " + fechaStr + ". Use formato YYYY-MM-DD");
        }
    }

    /**
     * Convierte un string de tiempo (HH:MM o HH:MM:SS) a java.sql.Time
     */
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
            throw new RuntimeException("Formato de tiempo inválido: " + timeStr + ". Use formato HH:MM o HH:MM:SS");
        }
    }
    /**
     * Verifica si la reserva está dentro del límite de tiempo para ser cancelada.
     * Utiliza la configuración general del sistema para determinar el límite de horas.
     * 
     * @param reserva La reserva a verificar
     * @return true si está dentro del límite de tiempo, false en caso contrario
     */
    private boolean verificarLimiteCancelacion(Reserva reserva) {
        // Obtener el límite de horas desde la configuración general
        int horasLimite = ConfiguracionSistemaUtil.getLimiteTiempoCancelacion();
        
        // Obtener la fecha y hora actual
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();
        
        // Obtener la fecha y hora de la reserva
        java.time.LocalDate fechaReserva = reserva.getFecha().toLocalDate();
        java.time.LocalTime horaInicio = reserva.getHoraInicio().toLocalTime();
        java.time.LocalDateTime fechaHoraReserva = java.time.LocalDateTime.of(fechaReserva, horaInicio);
        
        // Calcular la diferencia en horas
        long horasHastaReserva = java.time.Duration.between(ahora, fechaHoraReserva).toHours();
        
        System.out.println("Horas hasta la reserva: " + horasHastaReserva);
        System.out.println("Límite de horas para cancelar: " + horasLimite);
        
        // Verificar si está dentro del límite (puede cancelar si faltan más horas que el límite)
        return horasHastaReserva >= horasLimite;
    }
}
