package com.example.deporsm.controller;

import com.example.deporsm.dto.CrearPagoDTO;
import com.example.deporsm.model.Pago;
import com.example.deporsm.repository.PagoRepository;
import com.example.deporsm.service.PagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/pagos")
@CrossOrigin(origins = "https://deporsm-apiwith-1035693188565.us-central1.run.app", allowCredentials = "true")
public class PagoController {

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
}
