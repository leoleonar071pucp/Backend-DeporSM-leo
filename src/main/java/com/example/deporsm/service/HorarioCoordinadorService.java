package com.example.deporsm.service;

import com.example.deporsm.dto.HorarioCoordinadorDTO;
import com.example.deporsm.model.CoordinadorInstalacion;
import com.example.deporsm.model.HorarioCoordinador;
import com.example.deporsm.repository.CoordinadorInstalacionRepository;
import com.example.deporsm.repository.HorarioCoordinadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HorarioCoordinadorService {

    @Autowired
    private HorarioCoordinadorRepository horarioCoordinadorRepository;
    
    @Autowired
    private CoordinadorInstalacionRepository coordinadorInstalacionRepository;    public List<HorarioCoordinadorDTO> getHorariosByCoordinadorId(Integer usuarioId) {
        System.out.println("Buscando horarios para el usuario ID: " + usuarioId);
        try {
            List<HorarioCoordinador> horarios = horarioCoordinadorRepository.findHorariosCoordinadorByUsuarioId(usuarioId);
            System.out.println("Horarios encontrados en la base de datos: " + horarios.size());
            return mapToDTOs(horarios);
        } catch (Exception e) {
            System.err.println("Error al buscar horarios para el usuario ID " + usuarioId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al buscar horarios: " + e.getMessage(), e);
        }
    }
    
    public List<HorarioCoordinadorDTO> getHorariosByCoordinadorIdAndInstalacionId(Integer usuarioId, Integer instalacionId) {
        List<HorarioCoordinador> horarios = horarioCoordinadorRepository.findHorariosCoordinadorByUsuarioIdAndInstalacionId(usuarioId, instalacionId);
        return mapToDTOs(horarios);
    }    private List<HorarioCoordinadorDTO> mapToDTOs(List<HorarioCoordinador> horarios) {
        return horarios.stream().map(horario -> {
            try {
                HorarioCoordinadorDTO dto = new HorarioCoordinadorDTO();
                dto.setId(horario.getId());
                dto.setCoordinadorInstalacionId(horario.getCoordinadorInstalacion().getId());
                
                // Normalizar el nombre del día eliminando espacios extras y asegurándonos que sea consistente
                String diaSemana = horario.getDiaSemana();
                if (diaSemana != null) {
                    // Eliminar espacios al inicio y final
                    diaSemana = diaSemana.trim();
                    
                    // Normalizar días específicos
                    switch (diaSemana) {
                        case "martes ":
                            diaSemana = "martes";
                            break;
                        case "sab ado":
                        case "sábado":
                            diaSemana = "sabado";
                            break;
                        case "miercoles":
                        case "miércoles":
                            diaSemana = "miercoles";
                            break;
                    }
                } else {
                    diaSemana = "lunes"; // valor predeterminado si es null
                }
                
                dto.setDiaSemana(diaSemana);
                
                // Manejo seguro del formato de hora
                if (horario.getHoraInicio() != null) {
                    String horaInicio = horario.getHoraInicio().toString();
                    // Asegurarse que tiene formato HH:MM:SS
                    if (horaInicio.length() >= 5) {
                        dto.setHoraInicio(horaInicio);
                    } else {
                        dto.setHoraInicio("00:00:00");
                    }
                } else {
                    dto.setHoraInicio("00:00:00");
                }
                
                if (horario.getHoraFin() != null) {
                    String horaFin = horario.getHoraFin().toString();
                    // Asegurarse que tiene formato HH:MM:SS
                    if (horaFin.length() >= 5) {
                        dto.setHoraFin(horaFin);
                    } else {
                        dto.setHoraFin("00:00:00");
                    }
                } else {
                    dto.setHoraFin("00:00:00");
                }
                
                // Manejo seguro del nombre de la instalación
                String nombreInstalacion = "Instalación sin nombre";
                if (horario.getCoordinadorInstalacion() != null && 
                    horario.getCoordinadorInstalacion().getInstalacion() != null && 
                    horario.getCoordinadorInstalacion().getInstalacion().getNombre() != null) {
                    nombreInstalacion = horario.getCoordinadorInstalacion().getInstalacion().getNombre();
                }
                dto.setInstalacionNombre(nombreInstalacion);
                
                // Manejo seguro del ID de instalación
                Integer instalacionId = 0;
                if (horario.getCoordinadorInstalacion() != null && 
                    horario.getCoordinadorInstalacion().getInstalacion() != null && 
                    horario.getCoordinadorInstalacion().getInstalacion().getId() != null) {
                    instalacionId = horario.getCoordinadorInstalacion().getInstalacion().getId();
                }
                dto.setInstalacionId(instalacionId);
                
                System.out.println("Mapeado exitoso para horario ID: " + horario.getId() + 
                                  ", Instalación: " + dto.getInstalacionNombre() +
                                  ", Día: " + dto.getDiaSemana() +
                                  ", Horario: " + dto.getHoraInicio() + " - " + dto.getHoraFin());
                
                return dto;
            } catch (Exception e) {
                System.err.println("Error al mapear horario: " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        })
        .filter(dto -> dto != null)  // Filtrar cualquier mapeo nulo debido a errores
        .collect(Collectors.toList());
    }
}
