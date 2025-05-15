package com.example.deporsm.service;

import com.example.deporsm.dto.CrearReservaDTO;
import com.example.deporsm.dto.ReservaListDTO;
import com.example.deporsm.model.Instalacion;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.model.Usuario;
import com.example.deporsm.repository.InstalacionRepository;
import com.example.deporsm.repository.ReservaRepository;
import com.example.deporsm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservaService {

    @Autowired
    private ReservaRepository reservaRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private InstalacionRepository instalacionRepository;
    
    /**
     * Crea una nueva reserva para un usuario
     * @param email Email del usuario
     * @param reservaDTO Datos de la reserva
     * @return La reserva creada
     * @throws RuntimeException si no se encuentra el usuario o la instalación,
     *         o si la instalación no está disponible
     */
    public Reserva crearReserva(String email, CrearReservaDTO reservaDTO) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        Instalacion instalacion = instalacionRepository.findById(reservaDTO.getInstalacionId())
                .orElseThrow(() -> new RuntimeException("Instalación no encontrada"));
        
        if (!instalacion.getActivo()) {
            throw new RuntimeException("La instalación no está disponible para reservas");
        }
        
        // Verificar disponibilidad de horario (implementación simplificada)
        boolean horarioDisponible = verificarDisponibilidadHorario(
                instalacion.getId(), 
                reservaDTO.getFecha(),
                reservaDTO.getHoraInicio(),
                reservaDTO.getHoraFin());
        
        if (!horarioDisponible) {
            throw new RuntimeException("El horario seleccionado no está disponible");
        }
        
        // Crear la reserva
        Reserva reserva = new Reserva();
        reserva.setUsuario(usuario);
        reserva.setInstalacion(instalacion);
        reserva.setFecha(reservaDTO.getFecha());
        reserva.setHoraInicio(reservaDTO.getHoraInicio());
        reserva.setHoraFin(reservaDTO.getHoraFin());
        reserva.setNumeroAsistentes(reservaDTO.getNumeroAsistentes());
        reserva.setComentarios(reservaDTO.getComentarios());
        reserva.setEstado("pendiente"); // La reserva comienza en estado pendiente
        reserva.setEstadoPago("pendiente"); // El pago también comienza como pendiente
        
        // Guardar y devolver
        return reservaRepository.save(reserva);
    }
    
    /**
     * Cancela una reserva de un usuario
     * @param reservaId ID de la reserva
     * @param email Email del usuario
     * @param motivo Motivo de la cancelación
     * @throws RuntimeException si no se encuentra la reserva o si no pertenece al usuario
     */
    public void cancelarReserva(Integer reservaId, String email, String motivo) {
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
        
        // Verificar que la reserva no esté ya cancelada
        if ("cancelada".equals(reserva.getEstado())) {
            throw new RuntimeException("La reserva ya está cancelada");
        }
        
        // Actualizar el estado de la reserva
        reserva.setEstado("cancelada");
        reserva.setMotivo(motivo);
        
        // Guardar la reserva actualizada
        reservaRepository.save(reserva);
    }
    
    /**
     * Obtiene el historial de reservas de un usuario
     * @param email Email del usuario
     * @return Lista de reservas del usuario
     */
    public List<ReservaListDTO> obtenerHistorialReservas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        
        List<Reserva> reservas = reservaRepository.findByUsuario_Dni(usuario.getDni());
        
        return reservas.stream()
                .map(reserva -> {
                    ReservaListDTO dto = new ReservaListDTO(
                        reserva.getId(),
                        reserva.getUsuario().getNombre(),
                        reserva.getInstalacion().getNombre(),
                        reserva.getFecha(),
                        reserva.getHoraInicio(),
                        reserva.getHoraFin(),
                        reserva.getEstado(),
                        reserva.getEstadoPago()
                    );
                    return dto;
                })
                .collect(Collectors.toList());
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
        // Lógica simplificada - en un caso real, verificaríamos en la BD las reservas existentes
        // que se solapen con el horario solicitado
        
        // Para esta demostración, suponemos que está disponible
        return true;
    }
}
