package com.duoc.bancoxyz.model;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interes{

    private Long id;
    private String nombre;
    private BigDecimal saldo;
    private int edad;
    private String tipo; //ahorro, prestamo, hipoteca

}