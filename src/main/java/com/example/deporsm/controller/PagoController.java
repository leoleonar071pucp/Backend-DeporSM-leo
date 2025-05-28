package com.example.deporsm.controller;

import com.example.deporsm.dto.CrearPagoDTO;
import com.example.deporsm.model.Pago;
import com.example.deporsm.repository.PagoRepository;
import com.example.deporsm.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(
  origins = {
    "https://deporsm-apiwith-1035693188565.us-central1.run.app",
    "https://frontend-depor-sm-leo.vercel.app",
    "http://localhost:3000"
  },
  allowCredentials = "true"
)public class PagoController {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PagoService pagoService;

    @GetMapping("/reserva/{reservaId}")
    public ResponseEntity<?> obtenerPagoPorReserva(@PathVariable Integer reservaId) {
        return pagoRepository.findByReservaId(reservaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/online")
    public ResponseEntity<?> crearPagoOnline(@RequestBody CrearPagoDTO pagoDTO) {
        try {
            pagoService.crearPagoOnline(
                pagoDTO.getReservaId(),
                pagoDTO.getMonto(),
                pagoDTO.getReferenciaTransaccion(),
                pagoDTO.getUltimosDigitos()
            );
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el pago: " + e.getMessage());
        }
    }
    // Hola
      @PostMapping("/deposito")
    public ResponseEntity<?> crearPagoDeposito(
            @RequestParam("reservaId") Integer reservaId,
            @RequestParam("monto") BigDecimal monto,
            @RequestParam("comprobante") MultipartFile comprobante) {
        try {
            System.out.println("Recibida solicitud para crear pago con depósito - Reserva ID: " + reservaId);
            System.out.println("Nombre del archivo recibido: " + comprobante.getOriginalFilename());
            System.out.println("Tamaño del archivo: " + comprobante.getSize() + " bytes");

            String urlComprobante = pagoService.procesarPagoDeposito(reservaId, monto, comprobante);
            System.out.println("URL del comprobante generada: " + urlComprobante);

            return ResponseEntity.ok().body(urlComprobante);
        } catch (Exception e) {
            System.err.println("Error al procesar el pago por depósito: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al procesar el pago por depósito: " + e.getMessage());
        }
    }

    // Nuevo endpoint para comprobantes subidos a Supabase
    @PostMapping("/deposito-supabase")
    public ResponseEntity<?> crearPagoDepositoSupabase(@RequestBody PagoSupabaseRequest request) {
        try {
            System.out.println("Recibida solicitud para crear pago con comprobante de Supabase - Reserva ID: " + request.getReservaId());
            System.out.println("URL del comprobante en Supabase: " + request.getUrlComprobante());
            System.out.println("Monto: " + request.getMonto());

            String resultado = pagoService.procesarPagoDepositoSupabase(
                request.getReservaId(),
                request.getMonto(),
                request.getUrlComprobante()
            );

            System.out.println("Pago registrado exitosamente: " + resultado);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.err.println("Error al procesar pago con comprobante de Supabase: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al procesar el pago: " + e.getMessage());
        }
    }

    // Clase para el request del endpoint de Supabase
    public static class PagoSupabaseRequest {
        private Integer reservaId;
        private BigDecimal monto;
        private String urlComprobante;
        private String metodo;

        // Getters y setters
        public Integer getReservaId() { return reservaId; }
        public void setReservaId(Integer reservaId) { this.reservaId = reservaId; }

        public BigDecimal getMonto() { return monto; }
        public void setMonto(BigDecimal monto) { this.monto = monto; }

        public String getUrlComprobante() { return urlComprobante; }
        public void setUrlComprobante(String urlComprobante) { this.urlComprobante = urlComprobante; }

        public String getMetodo() { return metodo; }
        public void setMetodo(String metodo) { this.metodo = metodo; }
    }
}
