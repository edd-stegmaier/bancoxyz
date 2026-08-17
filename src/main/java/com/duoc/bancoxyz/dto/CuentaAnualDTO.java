package com.duoc.bancoxyz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class CuentaAnualDTO {

    private Long id;
    private LocalDate fecha;
    private String transaccion; // deposito o retiro
    private BigDecimal monto;
    private String descripcion;

}
