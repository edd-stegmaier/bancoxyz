package com.duoc.bancoxyz.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CuentaAnual {

    private Long id;
    private LocalDate fecha;
    private String transaccion; // deposito o retiro
    private BigDecimal monto;
    private String descripcion;

}
