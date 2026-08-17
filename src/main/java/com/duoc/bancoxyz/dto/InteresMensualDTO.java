package com.duoc.bancoxyz.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//interes MENSUAL sobre cuenta de AHORRO, PRESTAMO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InteresMensualDTO {
    
    private Long idCuenta;
    private String mes;
    private String anio;
    private BigDecimal interes;
    private BigDecimal saldoFinal;
}
