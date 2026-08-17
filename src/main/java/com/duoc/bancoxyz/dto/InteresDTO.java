package com.duoc.bancoxyz.dto;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class InteresDTO{

    private Long id;
    private String nombre;
    private BigDecimal saldo;
    private int edad;
    private String tipo;

}