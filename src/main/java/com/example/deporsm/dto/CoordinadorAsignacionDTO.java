package com.example.deporsm.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoordinadorAsignacionDTO {
    private Integer coordinadorId;
    private List<Integer> instalacionIds;
    private List<HorarioCoordinadorRequestDTO> horarios;
}
