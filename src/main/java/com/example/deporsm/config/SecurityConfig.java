package com.example.deporsm.config;

import com.example.deporsm.service.CustomUserDetailsService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class SecurityConfig {

    private final CorsFilter corsFilter;
    private final AuthDebugFilter authDebugFilter;

    public SecurityConfig(CorsFilter corsFilter, AuthDebugFilter authDebugFilter) {
        this.corsFilter = corsFilter;
        this.authDebugFilter = authDebugFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // Agregar el filtro de depuración primero
                .addFilterBefore(authDebugFilter, UsernamePasswordAuthenticationFilter.class)
                // Agregar el filtro CORS después
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)                .authorizeHttpRequests(auth -> auth
                        // Permitir OPTIONS sin autenticación (preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Permitir explícitamente el endpoint de logout
                        .requestMatchers("/api/auth/logout").permitAll()
                        // Asegurarse de que los recursos estáticos estén completamente permitidos
                        .requestMatchers("/comprobantes/**").permitAll()
                        .requestMatchers("/static/**").permitAll()
                        // Permitir todas las rutas sin autorización estricta (para depuración)
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // Establecer headers CORS explícitamente para la respuesta de logout
                            String origin = request.getHeader("Origin");
                            if (origin != null && (
                                origin.equals("https://deporsm-apiwith-1035693188565.us-central1.run.app") ||
                                origin.equals("https://frontend-depor-sm-leo.vercel.app") ||
                                origin.equals("http://localhost:3000")
                            )) {
                                response.setHeader("Access-Control-Allow-Origin", origin);
                            } else {
                                // Valor predeterminado para otros orígenes
                                response.setHeader("Access-Control-Allow-Origin", "https://frontend-depor-sm-leo.vercel.app");
                            }
                            response.setHeader("Access-Control-Allow-Credentials", "true");
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                )                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry())
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public CustomUserDetailsService customUserDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
    // Configuración para cookies
    @Bean
    public ServletContextInitializer cookieConfigurer() {
        return servletContext -> {
            var sessionCookieConfig = servletContext.getSessionCookieConfig();
            sessionCookieConfig.setHttpOnly(true);

            // Determinar si estamos en producción o desarrollo
            String activeProfile = System.getProperty("spring.profiles.active");
            boolean isProduction = activeProfile != null && activeProfile.contains("prod");

            // En producción: secure=true para HTTPS
            // En desarrollo: secure=false para HTTP local
            sessionCookieConfig.setSecure(isProduction);

            sessionCookieConfig.setMaxAge(86400); // 24 horas para mayor duración
            sessionCookieConfig.setName("JSESSIONID"); // Asegurarse que el nombre sea consistente
        };
    }

    // Configuración para permitir cookies en peticiones cross-origin
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            Rfc6265CookieProcessor processor = new Rfc6265CookieProcessor();

            // Determinar si estamos en producción o desarrollo
            String activeProfile = System.getProperty("spring.profiles.active");
            boolean isProduction = activeProfile != null && activeProfile.contains("prod");

            // En producción: SameSite=None para permitir cookies cross-site
            // En desarrollo: SameSite=Lax para mejor compatibilidad local
            processor.setSameSiteCookies(isProduction ? "None" : "Lax");

            // Imprimir información de depuración
            System.out.println("🍪 Configuración de cookies:");
            System.out.println("   - Perfil activo: " + (activeProfile != null ? activeProfile : "no definido"));
            System.out.println("   - Modo producción: " + isProduction);
            System.out.println("   - SameSite: " + (isProduction ? "None" : "Lax"));
            System.out.println("   - Secure: " + isProduction);

            context.setCookieProcessor(processor);
        });
    }
}