package com.duoc.bancoxyz.batch.processor;

import java.math.BigDecimal;

import org.springframework.batch.infrastructure.item.ItemProcessor;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;
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
        if (dto == null) {
            throw new BatchValidationException("Interés nulo");
        }
        if (validationHelper.duplicado(dto)) {
            throw new BatchValidationException("Interés duplicado: " + dto.getId());
        }

        BigDecimal saldo = dto.getSaldo();
        if (saldo == null) {
            saldo = ZERO;
        }

        if (saldo.compareTo(ZERO) <= 0) {
            throw new BatchValidationException("Saldo inválido para interés: " + dto.getId());
        }

        Integer edad = validationHelper.normalizeAge(dto.getEdad());
        if (edad == null) {
            throw new BatchValidationException("Edad inválida para interés: " + dto.getId());
        }

        return new Interes(
                dto.getId(),
                validationHelper.textoRequerido(dto.getNombre(), "SIN_NOMBRE"),
                saldo.abs(),
                edad,
                validationHelper.normalizeInteresTipo(dto.getTipo()));
    }
}