package com.duoc.bancoxyz.batch.processor;

import java.math.BigDecimal;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;
import com.duoc.bancoxyz.dto.CuentaAnualDTO;
import com.duoc.bancoxyz.model.CuentaAnual;

public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualDTO, CuentaAnual> {

    private final ItemValidator validationHelper;

    public CuentaAnualProcessor(ItemValidator validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Override
    public CuentaAnual process(CuentaAnualDTO dto) {
        if (dto == null) {
            throw new BatchValidationException("Cuenta anual nula");
        }
        if (validationHelper.duplicado(dto)) {
            throw new BatchValidationException("Cuenta anual duplicada: " + dto.getId());
        }

        BigDecimal monto = validationHelper.normalizeAmount(dto.getMonto());
        if (monto == null) {
            throw new BatchValidationException("Monto inválido para cuenta anual: " + dto.getId());
        }

        String transaccion = validationHelper.normalizeCuentaTransaccion(dto.getTransaccion());
        if (transaccion == null) {
            throw new BatchValidationException("Transacción inválida para cuenta anual: " + dto.getId());
        }

        return new CuentaAnual(
                dto.getId(),
                dto.getFecha(),
                transaccion,
                monto,
                validationHelper.textoRequerido(dto.getDescripcion(), "SIN_DESCRIPCION"));
    }
}