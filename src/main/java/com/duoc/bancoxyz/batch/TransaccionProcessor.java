package com.duoc.bancoxyz.batch;

import java.math.BigDecimal;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.duoc.bancoxyz.dto.TransaccionDTO;
import com.duoc.bancoxyz.model.Transaccion;

public class TransaccionProcessor implements ItemProcessor<TransaccionDTO, Transaccion> {

    private final ItemValidator validationHelper;

    public TransaccionProcessor(ItemValidator validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Override
    public Transaccion process(TransaccionDTO dto) {
        if (dto == null || validationHelper.duplicado(dto)) {
            return null;
        }

        BigDecimal monto = validationHelper.normalizeAmount(dto.getMonto());
        if (monto == null) {
            return null;
        }

        return new Transaccion(
                dto.getId(),
                dto.getFecha(),
                monto,
                validationHelper.normalizeTransaccionTipo(dto.getTipo()));
    }
}