package com.example.deporsm.config;

// This class has been deprecated in favor of CorsConfig
// Keeping the file to avoid compilation errors in case there are references to it
// All CORS configuration is now centralized in CorsConfig.java

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS configuration moved to CorsConfig.java
}
