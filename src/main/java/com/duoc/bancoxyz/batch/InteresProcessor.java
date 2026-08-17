package com.duoc.bancoxyz.batch;

import java.math.BigDecimal;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.duoc.bancoxyz.dto.InteresDTO;
import com.duoc.bancoxyz.model.Interes;

public class InteresProcessor implements ItemProcessor<InteresDTO, Interes> {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final ItemValidator validationHelper;

    public InteresProcessor(ItemValidator validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Override
    public Interes process(InteresDTO dto) {
        if (dto == null || validationHelper.duplicado(dto)) {
            return null;
        }

        BigDecimal saldo = dto.getSaldo();
        if (saldo == null) {
            saldo = ZERO;
        }

        if (saldo.compareTo(ZERO) <= 0) {
            return null;
        }

        Integer edad = validationHelper.normalizeAge(dto.getEdad());
        if (edad == null) {
            return null;
        }

        return new Interes(
                dto.getId(),
                validationHelper.textoRequerido(dto.getNombre(), "SIN_NOMBRE"),
                saldo.abs(),
                edad,
                validationHelper.normalizeInteresTipo(dto.getTipo()));
    }
}