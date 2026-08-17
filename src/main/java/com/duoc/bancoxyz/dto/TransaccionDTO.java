package com.duoc.bancoxyz.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data

public class TransaccionDTO {
    
    private int id;
    private LocalDate fecha;
    private BigDecimal monto;
    private String tipo; //debito o credito

}
