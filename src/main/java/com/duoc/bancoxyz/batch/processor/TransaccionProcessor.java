package com.duoc.bancoxyz.batch.processor;

import java.math.BigDecimal;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;
import com.duoc.bancoxyz.dto.TransaccionDTO;
import com.duoc.bancoxyz.model.Transaccion;

public class TransaccionProcessor implements ItemProcessor<TransaccionDTO, Transaccion> {

    private final ItemValidator validationHelper;

    public TransaccionProcessor(ItemValidator validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Override
    public Transaccion process(TransaccionDTO dto) {
        if (dto == null) {
            throw new BatchValidationException("Transacción nula");
        }
        if (validationHelper.duplicado(dto)) {
            throw new BatchValidationException("Transacción duplicada: " + dto.getId());
        }

        BigDecimal monto = validationHelper.normalizeAmount(dto.getMonto());
        if (monto == null) {
            throw new BatchValidationException("Monto inválido para transacción: " + dto.getId());
        }

        return new Transaccion(
                dto.getId(),
                dto.getFecha(),
                monto,
                validationHelper.normalizeTransaccionTipo(dto.getTipo()));
    }
}