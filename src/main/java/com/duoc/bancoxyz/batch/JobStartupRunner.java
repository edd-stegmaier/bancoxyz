package com.duoc.bancoxyz.batch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class JobStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JobStartupRunner.class);

    private final JobOperator jobOperator;
    private final Map<String, Job> jobs;

    @Value("${bancoxyz.jobs:all}")
    private String jobsProperty;

    public JobStartupRunner(JobOperator jobOperator, List<Job> jobBeans) {
        this.jobOperator = jobOperator;
        this.jobs = new LinkedHashMap<>();
        for (Job job : jobBeans) {
            this.jobs.put(job.getName(), job);
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> seleccion = resolverJobs();
        log.info("Jobs a ejecutar: {}", seleccion);

        for (String nombre : seleccion) {
            Job job = jobs.get(nombre);
            if (job == null) {
                throw new IllegalArgumentException(
                        "Job desconocido: " + nombre + ". Disponibles: " + jobs.keySet());
            }
            JobExecution execution = jobOperator.start(job, job.getJobParametersIncrementer()
                    .getNext(new org.springframework.batch.core.job.parameters.JobParameters()));
            log.info("Job {} termino con status {}", nombre, execution.getStatus());
        }
    }

    private List<String> resolverJobs() {
        String valor = jobsProperty == null ? "all" : jobsProperty.trim().toLowerCase(Locale.ROOT);
        if (valor.isBlank() || "all".equals(valor)) {
            return List.of("cuentasAnualesJob", "interesesJob", "transaccionesJob", "resumenesJob");
        }

        List<String> seleccion = new ArrayList<>();
        for (String token : valor.split(",")) {
            String nombre = token.trim();
            if (!nombre.isEmpty()) {
                seleccion.add(nombre);
            }
        }
        return seleccion;
    }
}
