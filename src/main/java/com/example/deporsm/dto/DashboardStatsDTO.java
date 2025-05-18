package com.example.deporsm.dto;

public interface DashboardStatsDTO {
    Long getTotalReservas();
    Long getReservasActivas();
    Long getTotalInstalaciones();
    Long getTotalObservaciones();
    Integer getMonthlyChangeTotalReservas();
    Integer getMonthlyChangeReservasActivas();
    Integer getMonthlyChangeTotalObservaciones();
}
