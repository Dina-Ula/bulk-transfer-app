package com.sw.config;


import com.sw.domain.Customer;
import com.sw.domain.CustomerClassifier;
import com.sw.domain.CustomerFieldSetMapper;
import com.sw.listener.JobListener;
import com.sw.repository.PersonRepoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;

import static com.sw.config.Constants.SWA_CONFLICT_FILE;
import static com.sw.config.Constants.SWW_EXCEPTION_FILE;
import static com.sw.config.Constants.SWW_INPUT_FILE;
import static com.sw.domain.Constants.Customer.Attribute.DISPLAY_NAME;
import static com.sw.domain.Constants.Customer.Attribute.NAME;
import static com.sw.domain.Constants.Customer.Attribute.USER_ID;


@Configuration
public class ApplicationJobConfig extends DefaultBatchConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationJobConfig.class);

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    public CustomerClassifier customerClassifier;

    @Autowired
    public JobListener jobListener;

    @Autowired
    public ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    public PersonRepoImpl personRepo;

    @Override
    public void setDataSource(DataSource dataSource) {
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Customer> itemReader(@Value("#{jobParameters['path']}") String path) {

        String fileName = path + SWW_INPUT_FILE;

        LOGGER.info("Reading from the file: {}", fileName);

        FlatFileItemReader<Customer> reader = new FlatFileItemReader<>();

        reader.setLinesToSkip(1);
        reader.setResource(new FileSystemResource(fileName));

        DefaultLineMapper<Customer> customerLineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("", "", "", "", "", "", "", "", "", NAME, "", "", "", "", DISPLAY_NAME, "", "", "", "", "",
                "", "", "", "", "", USER_ID, "", "", "", "", "", "", "", "");

        customerLineMapper.setLineTokenizer(tokenizer);
        customerLineMapper.setFieldSetMapper(new CustomerFieldSetMapper());
        customerLineMapper.afterPropertiesSet();

        reader.setLineMapper(customerLineMapper);

        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Customer> swaIntersectionItemWriter(@Value("#{jobParameters['path']}") String path) {

        String fileName = path + SWA_CONFLICT_FILE;

        LOGGER.info("Writing to the file: {}", fileName);

        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();

        writer.setResource(new FileSystemResource(fileName));

        writer.setAppendAllowed(true);

        writer.setLineAggregator(new DelimitedLineAggregator<Customer>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<Customer>() {
                    {
                        setNames(new String[]{USER_ID, NAME});
                    }
                });
            }
        });

        return writer;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<Customer> swwExceptionItemWriter(@Value("#{jobParameters['path']}") String path) {

        String fileName = path + SWW_EXCEPTION_FILE;

        LOGGER.info("Writing to the file: {}", fileName);

        FlatFileItemWriter<Customer> writer = new FlatFileItemWriter<>();

        writer.setResource(new FileSystemResource(fileName));

        writer.setAppendAllowed(true);

        writer.setLineAggregator(new DelimitedLineAggregator<Customer>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<Customer>() {
                    {
                        setNames(new String[]{USER_ID, NAME});
                    }
                });
            }
        });

        return writer;
    }

    @Bean
    public ClassifierCompositeItemWriter<Customer> itemWriter() {
        ClassifierCompositeItemWriter<Customer> itemWriter = new ClassifierCompositeItemWriter<>();

        itemWriter.setClassifier(customerClassifier);

        return itemWriter;
    }

    @Bean
    public Step bulkTransferStep() {
        return stepBuilderFactory.get("Bulk Transfer Step")
                .<Customer, Customer>chunk(10)
                .reader(itemReader(null))
                .writer(itemWriter())
                .stream(swaIntersectionItemWriter(null))
                .stream(swwExceptionItemWriter(null))
                .taskExecutor(threadPoolTaskExecutor)
                .build();
    }

    @Bean
    public Job bulkTransferJob() {
        return jobBuilderFactory.get("Bulk Transfer Job")
                .start(bulkTransferStep())
                .listener(jobListener)
                .build();
    }
}
