package com.example.deporsm.service;

import com.example.deporsm.model.Pago;
import com.example.deporsm.model.Reserva;
import com.example.deporsm.repository.PagoRepository;
import com.example.deporsm.repository.ReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private final Path comprobantesPath = Paths.get("comprobantes");    public PagoService() {
        try {
            // Crear el directorio para los comprobantes si no existe
            if (!Files.exists(comprobantesPath)) {
                Files.createDirectories(comprobantesPath);
                System.out.println("Directorio de comprobantes creado en: " + comprobantesPath.toAbsolutePath().toString());
            } else {
                System.out.println("Directorio de comprobantes ya existe en: " + comprobantesPath.toAbsolutePath().toString());
            }
        } catch (IOException e) {
            System.err.println("Error al crear directorio de comprobantes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo crear el directorio para los comprobantes", e);
        }
    }

    @Transactional
    public void crearPagoOnline(Integer reservaId, BigDecimal monto, String referencia, String ultimosDigitos) {        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        Pago pago = new Pago();
        pago.setReservaId(reservaId);
        pago.setMonto(monto);
        pago.setMetodo("online");
        pago.setEstado(reserva.getEstadoPago()); // Usar el estado de pago de la reserva
        pago.setReferenciaTransaccion(referencia);
        pago.setUltimosDigitos(ultimosDigitos);

        pagoRepository.save(pago);
    }

    @Transactional
    public String procesarPagoDeposito(Integer reservaId, BigDecimal monto, MultipartFile comprobante) {
        try {
            Reserva reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            // Generar un nombre único para el archivo
            String fileName = StringUtils.cleanPath(comprobante.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path targetLocation = comprobantesPath.resolve(uniqueFileName);            // Guardar el archivo
            try (InputStream inputStream = comprobante.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Comprobante guardado exitosamente en: " + targetLocation.toAbsolutePath().toString());
            }            // Crear el registro de pago
            Pago pago = new Pago();
            pago.setReservaId(reservaId);
            pago.setMonto(monto);
            pago.setMetodo("deposito");
            pago.setEstado(reserva.getEstadoPago()); // Usar el estado de pago de la reserva
            String urlComprobante = "/comprobantes/" + uniqueFileName;
            pago.setUrlComprobante(urlComprobante);
            System.out.println("URL de comprobante configurada: " + urlComprobante);

            Pago pagoGuardado = pagoRepository.save(pago);
            System.out.println("Pago registrado con ID: " + pagoGuardado.getId() + " para reserva ID: " + reservaId);

            return "/comprobantes/" + uniqueFileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al procesar el comprobante de pago", e);
        }
    }

    public Optional<Pago> obtenerPagoPorReserva(Integer reservaId) {
        return pagoRepository.findByReservaId(reservaId);
    }

    @Transactional
    public String procesarPagoDepositoSupabase(Integer reservaId, BigDecimal monto, String urlComprobante) {
        try {
            Reserva reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

            // Crear el registro de pago con la URL de Supabase
            Pago pago = new Pago();
            pago.setReservaId(reservaId);
            pago.setMonto(monto);
            pago.setMetodo("deposito");
            pago.setEstado(reserva.getEstadoPago()); // Usar el estado de pago de la reserva
            pago.setUrlComprobante(urlComprobante); // URL de Supabase

            System.out.println("URL de comprobante de Supabase configurada: " + urlComprobante);

            Pago pagoGuardado = pagoRepository.save(pago);
            System.out.println("Pago registrado con ID: " + pagoGuardado.getId() + " para reserva ID: " + reservaId);

            return urlComprobante;
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el comprobante de pago de Supabase", e);
        }
    }
}
