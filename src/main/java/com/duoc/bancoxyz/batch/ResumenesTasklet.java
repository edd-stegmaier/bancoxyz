package com.duoc.bancoxyz.batch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.duoc.bancoxyz.dto.EstadoCuentaDTO;
import com.duoc.bancoxyz.dto.InteresMensualDTO;
import com.duoc.bancoxyz.dto.ResumenTransaccionesDTO;

@Component
public class ResumenesTasklet implements Tasklet {

    private final JdbcTemplate jdbcTemplate;

    public ResumenesTasklet(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        crearTablasResumen();
        limpiarTablasResumen();
        guardarEstadoCuentaResumen();
        guardarInteresMensualResumen();
        guardarResumenTransacciones();
        return RepeatStatus.FINISHED;
    }

    private void crearTablasResumen() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS estado_cuenta_resumen (
                    id_cuenta BIGINT,
                    anio BIGINT,
                    numero_depositos BIGINT,
                    numero_retiros BIGINT,
                    numero_compras BIGINT,
                    total_ingresos BIGINT,
                    total_egresos BIGINT,
                    saldo DECIMAL(19,2)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS interes_mensual_resumen (
                    id_cuenta BIGINT,
                    mes VARCHAR(20),
                    anio VARCHAR(10),
                    interes DECIMAL(19,2),
                    saldo_final DECIMAL(19,2)
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS resumen_transacciones (
                    id_resumen BIGINT,
                    fecha DATE,
                    transacciones_aprobadas BIGINT,
                    transacciones_invalidas BIGINT,
                    transacciones_debito BIGINT,
                    transacciones_credito BIGINT,
                    monto_total DECIMAL(19,2)
                )
                """);
    }

    private void limpiarTablasResumen() {
        jdbcTemplate.update("DELETE FROM estado_cuenta_resumen");
        jdbcTemplate.update("DELETE FROM interes_mensual_resumen");
        jdbcTemplate.update("DELETE FROM resumen_transacciones");
    }

    private void guardarEstadoCuentaResumen() {
        List<EstadoCuentaDTO> resumenes = jdbcTemplate.query("""
                SELECT
                    id AS id_cuenta,
                    EXTRACT(YEAR FROM fecha) AS anio,
                    SUM(CASE WHEN transaccion = 'deposito' THEN 1 ELSE 0 END) AS numero_depositos,
                    SUM(CASE WHEN transaccion = 'retiro' THEN 1 ELSE 0 END) AS numero_retiros,
                    SUM(CASE WHEN LOWER(descripcion) LIKE '%compra%' THEN 1 ELSE 0 END) AS numero_compras,
                    SUM(CASE WHEN transaccion = 'deposito' THEN monto ELSE 0 END) AS total_ingresos,
                    SUM(CASE WHEN transaccion = 'retiro' THEN monto ELSE 0 END) AS total_egresos
                FROM cuentas_anuales
                GROUP BY id, EXTRACT(YEAR FROM fecha)
                ORDER BY id, anio
                """, this::mapEstadoCuentaResumen);

        for (EstadoCuentaDTO resumen : resumenes) {
            jdbcTemplate.update("""
                    INSERT INTO estado_cuenta_resumen
                    (id_cuenta, anio, numero_depositos, numero_retiros, numero_compras, total_ingresos, total_egresos, saldo)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    resumen.getIdCuenta(),
                    resumen.getAnio(),
                    resumen.getNumeroDepositos(),
                    resumen.getNumeroRetiros(),
                    resumen.getNumeroCompras(),
                    resumen.getTotalIngresos(),
                    resumen.getTotalEgresos(),
                    resumen.getSaldo());
        }
    }

    private void guardarInteresMensualResumen() {
        LocalDate hoy = LocalDate.now();
        String mes = String.valueOf(hoy.getMonthValue());
        String anio = String.valueOf(hoy.getYear());

        List<InteresMensualDTO> resumenes = jdbcTemplate.query("""
                SELECT id, saldo, tipo
                FROM intereses
                ORDER BY id
                """, (resultSet, rowNum) -> mapInteresMensualResumen(resultSet, mes, anio));

        for (InteresMensualDTO resumen : resumenes) {
            jdbcTemplate.update("""
                    INSERT INTO interes_mensual_resumen
                    (id_cuenta, mes, anio, interes, saldo_final)
                    VALUES (?, ?, ?, ?, ?)
                    """,
                    resumen.getIdCuenta(),
                    resumen.getMes(),
                    resumen.getAnio(),
                    resumen.getInteres(),
                    resumen.getSaldoFinal());
        }
    }

    private void guardarResumenTransacciones() {
        List<ResumenTransaccionesDTO> resumenes = jdbcTemplate.query("""
                SELECT
                    fecha,
                    COUNT(*) AS transacciones_aprobadas,
                    SUM(CASE WHEN tipo = 'debito' THEN 1 ELSE 0 END) AS transacciones_debito,
                    SUM(CASE WHEN tipo = 'credito' THEN 1 ELSE 0 END) AS transacciones_credito,
                    SUM(monto) AS monto_total
                FROM transacciones
                GROUP BY fecha
                ORDER BY fecha
                """, this::mapResumenTransacciones);

        long idResumen = 1L;
        for (ResumenTransaccionesDTO resumen : resumenes) {
            resumen.setIdResumen(idResumen++);

            jdbcTemplate.update("""
                    INSERT INTO resumen_transacciones
                    (id_resumen, fecha, transacciones_aprobadas, transacciones_invalidas, transacciones_debito, transacciones_credito, monto_total)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """,
                    resumen.getIdResumen(),
                    resumen.getFecha(),
                    resumen.getTransaccionesAprobadas(),
                    resumen.getTransaccionesDebito(),
                    resumen.getTransaccionesCredito(),
                    resumen.getMontoTotal());
        }
    }

    private EstadoCuentaDTO mapEstadoCuentaResumen(ResultSet resultSet, int rowNum) throws SQLException {
        long ingresos = resultSet.getLong("total_ingresos");
        long egresos = resultSet.getLong("total_egresos");

        return new EstadoCuentaDTO(
                resultSet.getLong("id_cuenta"),
                resultSet.getLong("anio"),
                resultSet.getLong("numero_depositos"),
                resultSet.getLong("numero_retiros"),
                resultSet.getLong("numero_compras"),
                ingresos,
                egresos,
                BigDecimal.valueOf(ingresos).subtract(BigDecimal.valueOf(egresos)));
    }

    private InteresMensualDTO mapInteresMensualResumen(ResultSet resultSet, String mes, String anio)
            throws SQLException {
        BigDecimal saldo = resultSet.getBigDecimal("saldo");
        BigDecimal tasa = calcularTasaInteres(resultSet.getString("tipo"));
        BigDecimal interes = saldo.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

        return new InteresMensualDTO(
                resultSet.getLong("id"),
                mes,
                anio,
                interes,
                saldo.add(interes).setScale(2, RoundingMode.HALF_UP));
    }

    private ResumenTransaccionesDTO mapResumenTransacciones(ResultSet resultSet, int rowNum) throws SQLException {
        return new ResumenTransaccionesDTO(
                null,
                resultSet.getDate("fecha").toLocalDate(),
                resultSet.getLong("transacciones_aprobadas"),
                resultSet.getLong("transacciones_debito"),
                resultSet.getLong("transacciones_credito"),
                resultSet.getBigDecimal("monto_total").setScale(2, RoundingMode.HALF_UP));
    }

    private BigDecimal calcularTasaInteres(String tipo) {
        String normalizado = tipo == null ? "ahorro" : tipo.trim().toLowerCase(Locale.ROOT);

        return switch (normalizado) {
            case "prestamo" -> new BigDecimal("0.025");
            case "hipoteca" -> new BigDecimal("0.018");
            default -> new BigDecimal("0.015");
        };
    }
}