package com.duoc.bancoxyz.batch.listener;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio para registrar tiempos de ejecución de Job y Steps
 * para análisis de rendimiento y optimización de configuraciones.
 * 
 * Se integra como bean inyectable en los steps y jobs.
 */
public class BatchExecutionTimeListener {

    private static final Logger log = LoggerFactory.getLogger(BatchExecutionTimeListener.class);

    // Almacena los tiempos de inicio de cada step para calcular duración
    private final ConcurrentHashMap<String, Long> stepStartTimes = new ConcurrentHashMap<>();

    public void logJobStart(String jobName, long jobId) {
        log.info("==================== INICIANDO JOB ====================");
        log.info("Job: {}", jobName);
        log.info("JobId: {}", jobId);
        log.info("Timestamp: {}", System.currentTimeMillis());
    }

    public void logJobFinish(String jobName, String status, long startTime, long endTime) {
        long duration = endTime - startTime;

        log.info("==================== FINALIZANDO JOB ====================");
        log.info("Job: {}", jobName);
        log.info("Status: {}", status);
        log.info("Duración total: {} ms ({} segundos)", duration, duration / 1000);
        log.info("=====================================================");
    }

    public void logStepStart(String stepName) {
        String key = stepName;
        stepStartTimes.put(key, System.currentTimeMillis());
        log.info("→ Iniciando Step: {}", stepName);
    }

    public void logStepFinish(String stepName, int processCount, int skipCount, int writeCount) {
        Long startTime = stepStartTimes.remove(stepName);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ Finalizó Step: {} | Duración: {} ms | Items: {} | Skips: {} | Escrituras: {}",
                    stepName,
                    duration,
                    processCount,
                    skipCount,
                    writeCount);
        }
    }

    public void logJobSummary(java.util.List<StepSummary> steps) {
        log.info("--- RESUMEN DE STEPS ---");
        for (StepSummary step : steps) {
            log.info("  Step: {} | Status: {} | Duración: {} ms | Items procesados: {} | Items saltados: {}",
                    step.getName(),
                    step.getStatus(),
                    step.getDuration(),
                    step.getProcessCount(),
                    step.getSkipCount());
        }
    }

    /**
     * Clase auxiliar para resumir información de un step
     */
    public static class StepSummary {
        private final String name;
        private final String status;
        private final long duration;
        private final int processCount;
        private final int skipCount;

        public StepSummary(String name, String status, long duration, int processCount, int skipCount) {
            this.name = name;
            this.status = status;
            this.duration = duration;
            this.processCount = processCount;
            this.skipCount = skipCount;
        }

        public String getName() { return name; }
        public String getStatus() { return status; }
        public long getDuration() { return duration; }
        public int getProcessCount() { return processCount; }
        public int getSkipCount() { return skipCount; }
    }
}
