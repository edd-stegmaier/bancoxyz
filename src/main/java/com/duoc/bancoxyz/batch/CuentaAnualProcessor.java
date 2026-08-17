package com.duoc.bancoxyz.batch;

import java.math.BigDecimal;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.duoc.bancoxyz.dto.CuentaAnualDTO;
import com.duoc.bancoxyz.model.CuentaAnual;

public class CuentaAnualProcessor implements ItemProcessor<CuentaAnualDTO, CuentaAnual> {

    private final ItemValidator validationHelper;

    public CuentaAnualProcessor(ItemValidator validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Override
    public CuentaAnual process(CuentaAnualDTO dto) {
        if (dto == null || validationHelper.duplicado(dto)) {
            return null;
        }

        BigDecimal monto = validationHelper.normalizeAmount(dto.getMonto());
        if (monto == null) {
            return null;
        }

        String transaccion = validationHelper.normalizeCuentaTransaccion(dto.getTransaccion());
        if (transaccion == null) {
            return null;
        }

        return new CuentaAnual(
                dto.getId(),
                dto.getFecha(),
                transaccion,
                monto,
                validationHelper.textoRequerido(dto.getDescripcion(), "SIN_DESCRIPCION"));
    }
}