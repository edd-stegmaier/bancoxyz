package com.duoc.bancoxyz.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaccion {
    
    private int id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo; //debito o credito

}
