package com.duoc.bancoxyz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//Resumen transacciones DIARIAS de un cliente
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResumenTransaccionesDTO {
    
    //private Long idCuenta;
    private Long idResumen;
    private LocalDate fecha;
    private Long transaccionesAprobadas;
    private Long transaccionesDebito;
    private Long transaccionesCredito;
    private BigDecimal montoTotal;

}