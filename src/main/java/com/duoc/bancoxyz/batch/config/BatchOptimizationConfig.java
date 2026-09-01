package com.duoc.bancoxyz.batch.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de optimización de Spring Batch con perfiles paramétricos
 * Permite ajustar CHUNK_SIZE, corePoolSize y maxPoolSize según el ambiente
 */
@Configuration
@ConfigurationProperties(prefix = "bancoxyz.batch")
public class BatchOptimizationConfig {

    /**
     * CHUNK_SIZE: número de items a procesar en cada chunk
     * 
     * Valores recomendados:
     * - Desarrollo/Test: 10 (bajo para facilitar debugging)
     * - Ambiente local optimizado: 50-100 (balance)
     * - Producción ligera: 100 (más throughput, menos commits)
     * - Producción pesada: 200+ (máximo throughput, requiere más memoria)
     * 
     * Consideración: tamaño de fila × CHUNK_SIZE ≤ memoria disponible en JVM
     */
    private int chunkSize = 10;

    /**
     * Core pool size para TaskExecutor
     * Número de hilos iniciales del pool de threads
     */
    private int corePoolSize = 2;

    /**
     * Max pool size para TaskExecutor
     * Número máximo de hilos que puede crear el pool
     */
    private int maxPoolSize = 4;

    /**
     * Queue capacity para TaskExecutor
     * Número de tareas que pueden quedar en espera
     */
    private int queueCapacity = 100;

    /**
     * Timeout en segundos para que los threads terminen gracefully
     */
    private int awaitTerminationSeconds = 60;

    /**
     * Retry limit: número máximo de reintentos para excepciones recuperables
     */
    private int retryLimit = 3;

    /**
     * Skip limit: número máximo de items que pueden ser saltados
     */
    private int skipLimit = 20;

    /**
     * Initial interval para ExponentialBackOffPolicy (en milisegundos)
     */
    private long backOffInitialInterval = 200L;

    /**
     * Multiplier para ExponentialBackOffPolicy
     */
    private double backOffMultiplier = 2.0;

    /**
     * Max interval para ExponentialBackOffPolicy (en milisegundos)
     */
    private long backOffMaxInterval = 2000L;

    // Getters y setters
    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getQueueCapacity() {
        return queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getAwaitTerminationSeconds() {
        return awaitTerminationSeconds;
    }

    public void setAwaitTerminationSeconds(int awaitTerminationSeconds) {
        this.awaitTerminationSeconds = awaitTerminationSeconds;
    }

    public int getRetryLimit() {
        return retryLimit;
    }

    public void setRetryLimit(int retryLimit) {
        this.retryLimit = retryLimit;
    }

    public int getSkipLimit() {
        return skipLimit;
    }

    public void setSkipLimit(int skipLimit) {
        this.skipLimit = skipLimit;
    }

    public long getBackOffInitialInterval() {
        return backOffInitialInterval;
    }

    public void setBackOffInitialInterval(long backOffInitialInterval) {
        this.backOffInitialInterval = backOffInitialInterval;
    }

    public double getBackOffMultiplier() {
        return backOffMultiplier;
    }

    public void setBackOffMultiplier(double backOffMultiplier) {
        this.backOffMultiplier = backOffMultiplier;
    }

    public long getBackOffMaxInterval() {
        return backOffMaxInterval;
    }

    public void setBackOffMaxInterval(long backOffMaxInterval) {
        this.backOffMaxInterval = backOffMaxInterval;
    }
}
