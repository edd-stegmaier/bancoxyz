package com.duoc.bancoxyz.batch.config;

import java.time.LocalDate;

import javax.sql.DataSource;

import com.duoc.bancoxyz.batch.exception.BatchValidationException;
import com.duoc.bancoxyz.batch.listener.BatchExecutionTimeListener;
import com.duoc.bancoxyz.batch.policy.BatchSkipPolicy;
import com.duoc.bancoxyz.batch.processor.CuentaAnualProcessor;
import com.duoc.bancoxyz.batch.processor.InteresProcessor;
import com.duoc.bancoxyz.batch.processor.ItemValidator;
import com.duoc.bancoxyz.batch.processor.TransaccionProcessor;
import com.duoc.bancoxyz.batch.tasklet.ResumenesTasklet;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.Step;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.io.Resource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.duoc.bancoxyz.dto.*;
import com.duoc.bancoxyz.model.*;

@Configuration
public class BatchConfig {

    private final BatchOptimizationConfig optimizationConfig;

    public BatchConfig(BatchOptimizationConfig optimizationConfig) {
        this.optimizationConfig = optimizationConfig;
    }

    @Bean
    public BatchExecutionTimeListener batchExecutionTimeListener() {
        return new BatchExecutionTimeListener();
    }

    @Value("${bancoxyz.archivo-cuentas-anuales:classpath:data/cuentas_anuales.csv}")
    private Resource archivoCuentasAnuales;

    @Value("${bancoxyz.archivo-intereses:classpath:data/intereses.csv}")
    private Resource archivoIntereses;

    @Value("${bancoxyz.archivo-transacciones:classpath:data/transacciones.csv}")
    private Resource archivoTransacciones;

    @Bean
    public FlatFileItemReader<CuentaAnualDTO> cuentasAnualesItemReader() {
        return createReader("cuentasAnualesItemReader", archivoCuentasAnuales, cuentasAnualesValidationHelper(),
                CuentaAnualDTO.class,
                "id", "fecha", "transaccion", "monto", "descripcion");
    }

    @Bean
    public FlatFileItemReader<InteresDTO> interesesItemReader() {
        return createReader("interesesItemReader", archivoIntereses, cuentasAnualesValidationHelper(),
                InteresDTO.class,
                "id", "nombre", "saldo", "edad", "tipo");
    }

    @Bean
    public FlatFileItemReader<TransaccionDTO> transaccionesItemReader() {
        return createReader("transaccionesItemReader", archivoTransacciones, cuentasAnualesValidationHelper(),
                TransaccionDTO.class,
                "id", "fecha", "monto", "tipo");
    }

    @Bean
    public ItemValidator cuentasAnualesValidationHelper() {
        return new ItemValidator();
    }

    @Bean
    public CuentaAnualProcessor cuentasAnualesProcessor(ItemValidator validationHelper) {
        return new CuentaAnualProcessor(validationHelper);
    }

    @Bean
    public InteresProcessor interesesProcessor(ItemValidator validationHelper) {
        return new InteresProcessor(validationHelper);
    }

    @Bean
    public TransaccionProcessor transaccionesProcessor(ItemValidator validationHelper) {
        return new TransaccionProcessor(validationHelper);
    }

    @Bean
    public JdbcBatchItemWriter<CuentaAnual> cuentasAnualesItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<CuentaAnual>()
                .dataSource(dataSource)
                .sql("INSERT INTO cuentas_anuales "
                        + "(id, fecha, transaccion, monto, descripcion) "
                        + "VALUES (:id, :fecha, :transaccion, :monto, :descripcion)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Interes> interesesItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Interes>()
                .dataSource(dataSource)
                .sql("INSERT INTO intereses "
                        + "(id, nombre, saldo, edad, tipo) "
                        + "VALUES (:id, :nombre, :saldo, :edad, :tipo)")
                .beanMapped()
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Transaccion> transaccionesItemWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Transaccion>()
                .dataSource(dataSource)
                .sql("INSERT INTO transacciones "
                        + "(id, fecha, monto, tipo) "
                        + "VALUES (:id, :fecha, :monto, :tipo)")
                .beanMapped()
                .build();
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new JdbcTransactionManager(dataSource);
    }

    @Bean
    public Step cuentasAnualesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<CuentaAnualDTO> cuentasAnualesItemReader,
            CuentaAnualProcessor cuentasAnualesProcessor,
            JdbcBatchItemWriter<CuentaAnual> cuentasAnualesItemWriter,
            TaskExecutor batchTaskExecutor) {
        return new StepBuilder("cuentasAnualesStep", jobRepository)
                .<CuentaAnualDTO, CuentaAnual>chunk(optimizationConfig.getChunkSize(), transactionManager)
                .reader(cuentasAnualesItemReader)
                .processor(cuentasAnualesProcessor)
                .writer(cuentasAnualesItemWriter)
                .taskExecutor(batchTaskExecutor)
                .faultTolerant()
                .retry(BatchValidationException.class)
                .retryLimit(optimizationConfig.getRetryLimit())
                .backOffPolicy(exponentialBackOffPolicy())
                .skipPolicy(new BatchSkipPolicy())
                .build();
    }

    @Bean
    public Step interesesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<InteresDTO> interesesItemReader,
            InteresProcessor interesesProcessor,
            JdbcBatchItemWriter<Interes> interesesItemWriter,
            TaskExecutor batchTaskExecutor) {
        return new StepBuilder("interesesStep", jobRepository)
                .<InteresDTO, Interes>chunk(optimizationConfig.getChunkSize(), transactionManager)
                .reader(interesesItemReader)
                .processor(interesesProcessor)
                .writer(interesesItemWriter)
                .taskExecutor(batchTaskExecutor)
                .faultTolerant()
                .retry(BatchValidationException.class)
                .retryLimit(optimizationConfig.getRetryLimit())
                .backOffPolicy(exponentialBackOffPolicy())
                .skipPolicy(new BatchSkipPolicy())
                .build();
    }

    @Bean
    public Step transaccionesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<TransaccionDTO> transaccionesItemReader,
            TransaccionProcessor transaccionesProcessor,
            JdbcBatchItemWriter<Transaccion> transaccionesItemWriter,
            TaskExecutor batchTaskExecutor) {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionDTO, Transaccion>chunk(optimizationConfig.getChunkSize(), transactionManager)
                .reader(transaccionesItemReader)
                .processor(transaccionesProcessor)
                .writer(transaccionesItemWriter)
                .taskExecutor(batchTaskExecutor)
                .faultTolerant()
                .retry(BatchValidationException.class)
                .retryLimit(optimizationConfig.getRetryLimit())
                .backOffPolicy(exponentialBackOffPolicy())
                .skipPolicy(new BatchSkipPolicy())
                .build();
    }

    @Bean
    public Step resumenesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ResumenesTasklet resumenesH2Tasklet) {
        return new StepBuilder("resumenesStep", jobRepository)
                .tasklet(resumenesH2Tasklet, transactionManager)
                .build();
    }

    @Bean
    public ExponentialBackOffPolicy exponentialBackOffPolicy() {
        ExponentialBackOffPolicy policy = new ExponentialBackOffPolicy();
        policy.setInitialInterval(optimizationConfig.getBackOffInitialInterval());
        policy.setMultiplier(optimizationConfig.getBackOffMultiplier());
        policy.setMaxInterval(optimizationConfig.getBackOffMaxInterval());
        return policy;
    }

    @Bean
    public Job bancoxyzPipelineJob(JobRepository jobRepository,
            Step cuentasAnualesStep,
            Step interesesStep,
            Step transaccionesStep,
            Step resumenesStep) {
        return new JobBuilder("bancoxyzPipelineJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cuentasAnualesStep)
                .next(interesesStep)
                .next(transaccionesStep)
                .next(resumenesStep)
                .build();
    }

    @Bean
    public Job cuentasAnualesJob(JobRepository jobRepository, Step cuentasAnualesStep) {
        return new JobBuilder("cuentasAnualesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cuentasAnualesStep)
                .build();
    }

    @Bean
    public Job interesesJob(JobRepository jobRepository, Step interesesStep) {
        return new JobBuilder("interesesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(interesesStep)
                .build();
    }

    @Bean
    public Job transaccionesJob(JobRepository jobRepository, Step transaccionesStep) {
        return new JobBuilder("transaccionesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(transaccionesStep)
                .build();
    }

    @Bean
    public Job resumenesJob(JobRepository jobRepository, Step resumenesStep) {
        return new JobBuilder("resumenesJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(resumenesStep)
                .build();
    }

    private <T> FlatFileItemReader<T> createReader(String name, Resource resource,
            ItemValidator validationHelper, Class<T> targetType,
            String... fieldNames) {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames(fieldNames);

        BeanWrapperFieldSetMapper<T> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(targetType);

        DefaultConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(String.class, LocalDate.class,
                (Converter<String, LocalDate>) validationHelper::parseFlexibleDate);
        fieldSetMapper.setConversionService(conversionService);

        DefaultLineMapper<T> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<T>()
                .name(name)
                .resource(resource)
                .linesToSkip(1)
                .lineMapper(lineMapper)
                .build();
    }
}
