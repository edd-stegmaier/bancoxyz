package com.duoc.bancoxyz.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Estado de cuenta de un Cliente ANUAL
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstadoCuentaDTO {
    
    private Long idCuenta;
    private Long anio;
    private Long numeroDepositos;
    private Long numeroRetiros;
    private Long numeroCompras;
    private Long totalIngresos;
    private Long totalEgresos;
    private BigDecimal saldo;
}
