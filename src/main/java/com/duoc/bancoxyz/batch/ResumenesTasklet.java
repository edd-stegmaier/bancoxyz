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
import org.springframework.dao.DataAccessException;
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
        crearTablaSiNoExiste("""
                CREATE TABLE estado_cuenta_resumen (
                    id_cuenta NUMBER(19),
                    anio NUMBER(19),
                    numero_depositos NUMBER(19),
                    numero_retiros NUMBER(19),
                    numero_compras NUMBER(19),
                    total_ingresos NUMBER(19),
                    total_egresos NUMBER(19),
                    saldo NUMBER(19,2)
                )
                """);

        crearTablaSiNoExiste("""
                CREATE TABLE interes_mensual_resumen (
                    id_cuenta NUMBER(19),
                    mes VARCHAR2(20),
                    anio VARCHAR2(10),
                    interes NUMBER(19,2),
                    saldo_final NUMBER(19,2)
                )
                """);

        crearTablaSiNoExiste("""
                CREATE TABLE resumen_transacciones (
                    id_resumen NUMBER(19),
                    fecha DATE,
                    transacciones_aprobadas NUMBER(19),
                    transacciones_invalidas NUMBER(19),
                    transacciones_debito NUMBER(19),
                    transacciones_credito NUMBER(19),
                    monto_total NUMBER(19,2)
                )
                """);
    }

    private void crearTablaSiNoExiste(String ddl) {
        try {
            jdbcTemplate.execute(ddl);
        } catch (DataAccessException ex) {
            Throwable root = ex.getMostSpecificCause();
            String message = root.getMessage() == null ? "" : root.getMessage();
            if (!message.contains("ORA-00955") && !message.toLowerCase(Locale.ROOT).contains("already exists")) {
                throw ex;
            }
        }
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
                    0L,
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
