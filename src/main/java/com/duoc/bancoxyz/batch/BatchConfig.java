package com.duoc.bancoxyz.batch;

import java.time.LocalDate;

import javax.sql.DataSource;

import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.Step;
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
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.duoc.bancoxyz.dto.*;
import com.duoc.bancoxyz.model.*;


@Configuration
public class BatchConfig {

    private static final int CHUNK_SIZE = 10;

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
            JdbcBatchItemWriter<CuentaAnual> cuentasAnualesItemWriter) {
        return new StepBuilder("cuentasAnualesStep", jobRepository)
                .<CuentaAnualDTO, CuentaAnual>chunk(CHUNK_SIZE, transactionManager)
                .reader(cuentasAnualesItemReader)
                .processor(cuentasAnualesProcessor)
                .writer(cuentasAnualesItemWriter)
                .build();
    }

    @Bean
    public Step interesesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<InteresDTO> interesesItemReader,
            InteresProcessor interesesProcessor,
            JdbcBatchItemWriter<Interes> interesesItemWriter) {
        return new StepBuilder("interesesStep", jobRepository)
                .<InteresDTO, Interes>chunk(CHUNK_SIZE, transactionManager)
                .reader(interesesItemReader)
                .processor(interesesProcessor)
                .writer(interesesItemWriter)
                .build();
    }

    @Bean
    public Step transaccionesStep(JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            FlatFileItemReader<TransaccionDTO> transaccionesItemReader,
            TransaccionProcessor transaccionesProcessor,
            JdbcBatchItemWriter<Transaccion> transaccionesItemWriter) {
        return new StepBuilder("transaccionesStep", jobRepository)
                .<TransaccionDTO, Transaccion>chunk(CHUNK_SIZE, transactionManager)
                .reader(transaccionesItemReader)
                .processor(transaccionesProcessor)
                .writer(transaccionesItemWriter)
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
    public Job bankDataJob(JobRepository jobRepository,
            Step cuentasAnualesStep,
            Step interesesStep,
            Step transaccionesStep,
            Step resumenesStep) {
        return new JobBuilder("bankDataJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(cuentasAnualesStep)
                .next(interesesStep)
                .next(transaccionesStep)
                .next(resumenesStep)
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
