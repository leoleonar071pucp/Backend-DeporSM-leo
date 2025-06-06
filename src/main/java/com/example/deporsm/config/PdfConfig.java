package com.example.deporsm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PdfConfig {
    
    @Value("${app.pdf.logo.simbolo:classpath:static/images/Simbolo_SanMiguel.png}")
    private String logoSimboloPath;
    
    @Value("${app.pdf.logo.municipalidad:classpath:static/images/Icono_Municipalidad_SanMiguel.png}")
    private String logoMunicipalidadPath;
    
    public String getLogoSimboloPath() {
        return logoSimboloPath;
    }
    
    public String getLogoMunicipalidadPath() {
        return logoMunicipalidadPath;
    }
}
