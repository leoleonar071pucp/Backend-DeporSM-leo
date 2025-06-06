package com.example.deporsm.util;

import com.example.deporsm.model.ConfiguracionGeneral;
import com.example.deporsm.service.ConfiguracionGeneralService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Clase utilitaria para acceder a la configuración general del sistema
 * desde cualquier parte de la aplicación.
 */
@Component
public class ConfiguracionSistemaUtil {
    
    private static ConfiguracionGeneralService configuracionService;
    
    @Autowired
    public ConfiguracionSistemaUtil(ConfiguracionGeneralService configuracionService) {
        ConfiguracionSistemaUtil.configuracionService = configuracionService;
    }
    
    /**
     * Obtiene el nombre del sitio configurado.
     * @return Nombre del sitio
     */
    public static String getNombreSitio() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getNombreSitio() : "DeporSM";
    }
    
    /**
     * Obtiene la descripción del sitio configurado.
     * @return Descripción del sitio
     */
    public static String getDescripcionSitio() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getDescripcionSitio() : "";
    }
    
    /**
     * Obtiene el teléfono de contacto configurado.
     * @return Teléfono de contacto
     */
    public static String getTelefonoContacto() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getTelefonoContacto() : "";
    }
    
    /**
     * Obtiene el email de contacto configurado.
     * @return Email de contacto
     */
    public static String getEmailContacto() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getEmailContacto() : "";
    }
    
    /**
     * Obtiene el máximo de reservas por usuario permitidas.
     * @return Número máximo de reservas por usuario
     */
    public static int getMaxReservasPorUsuario() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getMaxReservasPorUsuario() : 3;
    }
    
    /**
     * Obtiene el límite de tiempo para cancelar reservas (en horas).
     * @return Límite de tiempo en horas
     */
    public static int getLimiteTiempoCancelacion() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getLimiteTiempoCancelacion() : 48;
    }
    
    /**
     * Verifica si el modo mantenimiento está activo.
     * @return true si está en modo mantenimiento, false de lo contrario
     */
    public static boolean isModoMantenimiento() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getModoMantenimiento() : false;
    }
    
    /**
     * Verifica si el registro de usuarios está habilitado.
     * @return true si el registro está habilitado, false de lo contrario
     */
    public static boolean isRegistroHabilitado() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getRegistroHabilitado() : true;
    }
    
    /**
     * Verifica si las reservas están habilitadas.
     * @return true si las reservas están habilitadas, false de lo contrario
     */
    public static boolean isReservasHabilitadas() {
        ConfiguracionGeneral config = getConfiguracion();
        return config != null ? config.getReservasHabilitadas() : true;
    }
    
    /**
     * Obtiene la configuración completa del sistema.
     * @return Objeto de configuración general
     */
    private static ConfiguracionGeneral getConfiguracion() {
        if (configuracionService == null) {
            return null;
        }
        
        try {
            return configuracionService.getConfiguracion();
        } catch (Exception e) {
            System.err.println("Error al obtener configuración del sistema: " + e.getMessage());
            return null;
        }
    }
}
