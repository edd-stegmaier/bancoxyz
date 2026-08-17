package com.duoc.bancoxyz.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.duoc.bancoxyz.dto.CuentaAnualDTO;
import com.duoc.bancoxyz.dto.InteresDTO;
import com.duoc.bancoxyz.dto.TransaccionDTO;

public class ItemValidator {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter SLASH_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    private final Set<String> cuentasAnualesDuplicadas = ConcurrentHashMap.newKeySet();
    private final Set<String> interesesDuplicados = ConcurrentHashMap.newKeySet();
    private final Set<String> transaccionesDuplicadas = ConcurrentHashMap.newKeySet();

    public LocalDate parseFlexibleDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();

        try {
            return LocalDate.parse(trimmed, ISO_DATE);
        }
        catch (DateTimeParseException ignored) {
            try {
                return LocalDate.parse(trimmed, SLASH_DATE);
            }
            catch (DateTimeParseException exception) {
                throw new IllegalArgumentException("Formato de fecha invalido: " + value,
                        exception);
            }
        }
    }

    public String textoRequerido(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value.trim();
    }

    public BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) {
            return null;
        }

        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return amount.abs().setScale(amount.scale(), RoundingMode.HALF_UP);
    }

    public Integer normalizeAge(Integer age) {
        if (age == null) {
            return null;
        }

        if (age < 0 || age > 120) {
            return null;
        }

        return age;
    }

    public String normalizeCuentaTransaccion(String transaccion) {
        String normalized = textoRequerido(transaccion, null);
        if (normalized == null) {
            return null;
        }

        normalized = normalized.toLowerCase(Locale.ROOT);
        if ("compra".equals(normalized)) {
            return "retiro";
        }

        if ("deposito".equals(normalized) || "retiro".equals(normalized)) {
            return normalized;
        }

        return "retiro";
    }

    public String normalizeInteresTipo(String tipo) {
        String normalized = textoRequerido(tipo, "ahorro").toLowerCase(Locale.ROOT);
        if ("ahorro".equals(normalized) || "prestamo".equals(normalized) || "hipoteca".equals(normalized)) {
            return normalized;
        }

        return "ahorro";
    }

    public String normalizeTransaccionTipo(String tipo) {
        String normalized = textoRequerido(tipo, "debito").toLowerCase(Locale.ROOT);
        if ("debito".equals(normalized) || "credito".equals(normalized)) {
            return normalized;
        }

        return "debito";
    }

    public boolean duplicado(CuentaAnualDTO dto) {
        return !cuentasAnualesDuplicadas.add(buildKey(dto.getId(), dto.getFecha(), dto.getTransaccion(),
                dto.getMonto(), dto.getDescripcion()));
    }

    public boolean duplicado(InteresDTO dto) {
        return !interesesDuplicados.add(buildKey(dto.getId(), dto.getNombre(), dto.getSaldo(), dto.getEdad(),
                dto.getTipo()));
    }

    public boolean duplicado(TransaccionDTO dto) {
        return !transaccionesDuplicadas.add(buildKey(dto.getId(), dto.getFecha(), dto.getMonto(), dto.getTipo()));
    }

    private String buildKey(Object... values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            if (builder.length() > 0) {
                builder.append('|');
            }
            builder.append(value == null ? "<null>" : value.toString().trim());
        }

        return builder.toString();
    }
}