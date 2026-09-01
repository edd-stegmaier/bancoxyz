package com.duoc.bancoxyz.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;
import com.duoc.bancoxyz.batch.processor.CuentaAnualProcessor;
import com.duoc.bancoxyz.batch.processor.InteresProcessor;
import com.duoc.bancoxyz.batch.processor.ItemValidator;
import com.duoc.bancoxyz.batch.processor.TransaccionProcessor;
import com.duoc.bancoxyz.dto.CuentaAnualDTO;
import com.duoc.bancoxyz.dto.InteresDTO;
import com.duoc.bancoxyz.dto.TransaccionDTO;
import com.duoc.bancoxyz.model.CuentaAnual;
import com.duoc.bancoxyz.model.Interes;
import com.duoc.bancoxyz.model.Transaccion;

class BatchProcessorValidationTest {

    private final ItemValidator validator = new ItemValidator();

    @Test
    void cuentaAnualProcessorAcceptsValidData() {
        CuentaAnualProcessor processor = new CuentaAnualProcessor(validator);
        CuentaAnualDTO dto = new CuentaAnualDTO();
        dto.setId(10L);
        dto.setFecha(LocalDate.of(2024, 1, 15));
        dto.setTransaccion("deposito");
        dto.setMonto(new BigDecimal("2500.00"));
        dto.setDescripcion("Ingreso");

        CuentaAnual cuenta = processor.process(dto);

        assertNotNull(cuenta);
        assertEquals("deposito", cuenta.getTransaccion());
        assertEquals(new BigDecimal("2500.00"), cuenta.getMonto());
    }

    @Test
    void cuentaAnualProcessorRejectsInvalidBusinessData() {
        CuentaAnualProcessor processor = new CuentaAnualProcessor(validator);
        CuentaAnualDTO dto = new CuentaAnualDTO();
        dto.setId(10L);
        dto.setFecha(LocalDate.of(2024, 1, 15));
        dto.setTransaccion("deposito");
        dto.setMonto(BigDecimal.ZERO);
        dto.setDescripcion("Ingreso");

        assertThrows(BatchValidationException.class, () -> processor.process(dto));
    }

    @Test
    void interesProcessorRejectsInvalidAge() {
        InteresProcessor processor = new InteresProcessor(validator);
        InteresDTO dto = new InteresDTO();
        dto.setId(20L);
        dto.setNombre("Ana");
        dto.setSaldo(new BigDecimal("1500"));
        dto.setEdad(200);
        dto.setTipo("ahorro");

        assertThrows(BatchValidationException.class, () -> processor.process(dto));
    }

    @Test
    void transaccionProcessorTransformsType() {
        TransaccionProcessor processor = new TransaccionProcessor(validator);
        TransaccionDTO dto = new TransaccionDTO();
        dto.setId(30);
        dto.setFecha(LocalDate.of(2024, 1, 15));
        dto.setMonto(new BigDecimal("350.55"));
        dto.setTipo("credito");

        Transaccion transaccion = processor.process(dto);

        assertNotNull(transaccion);
        assertEquals("credito", transaccion.getTipo());
    }
}
