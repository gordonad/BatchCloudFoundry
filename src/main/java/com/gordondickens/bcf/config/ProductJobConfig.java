package com.gordondickens.bcf.config;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.repeat.support.TaskExecutorRepeatTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import com.gordondickens.bcf.batch.ProductItemWriter;
import com.gordondickens.bcf.entity.Product;

@Configuration
public class ProductJobConfig {
	private static final String IMPORT_PRODUCTS = "importProducts";

	private static final Logger logger = LoggerFactory
			.getLogger(ProductJobConfig.class);

	@Inject
	JobRepository jobRepository;

	@Inject
	DataSource dataSource;

	@Inject
	PlatformTransactionManager transactionManager;

	@Bean
	public TaskletStep simpleStep() throws Exception {
		TaskletStep bean = new TaskletStep();
		bean.setTransactionManager(transactionManager);
		bean.setJobRepository(jobRepository);
		bean.setStartLimit(100);
        bean.afterPropertiesSet();
        return bean;
	}

	@Bean
	public LineMapper<Product> productLineMapper() {
		DefaultLineMapper<Product> bean = new DefaultLineMapper<Product>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(',');
		tokenizer.setNames(new String[] { "productId", "store", "quantity",
				"description" });
		bean.setLineTokenizer(tokenizer);
		bean.setFieldSetMapper(new BeanWrapperFieldSetMapper<Product>());
        bean.afterPropertiesSet();
        return bean;

	}

	@Bean
	public FlatFileItemReader<Product> productReader() {
		FlatFileItemReader<Product> bean = new FlatFileItemReader<Product>();
		// TODO: Externalize resource reference
		ClassPathResource resource = new ClassPathResource(
				"testfiles/products1.txt");
		Assert.isTrue(resource != null, "Product Reader Resource is NULL");

		bean.setResource(resource);
		bean.setLinesToSkip(1);
		bean.setLineMapper(productLineMapper());
        return bean;
	}

	/**
	 * importProducts creates Job w Steps, Steps w Tasklet Step, Chunk Provider
	 * 
	 * @return
	 */
	@Bean
	public SimpleJob importProductsJob() throws Exception {
		// Create Job
		SimpleJob bean = new SimpleJob();
		bean.setName("importProductsJob");
		bean.setJobRepository(jobRepository);

		// Create Steps
		List<Step> steps = new ArrayList<Step>();

		// Create Tasklet Step
		TaskletStep step = simpleStep();
		step.setName(IMPORT_PRODUCTS);

		// TODO - Determine why repeat template is set on TaskletStep AND
		// ChunkProvider

		// Create repeat template for Chunk Size
		RepeatTemplate repeatTemplate = new TaskExecutorRepeatTemplate();
		repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(5));
		// TaskletStep <- RepeatTemplate
		step.setStepOperations(repeatTemplate);

		// Create Chunk Tasklet with Provider (reader) and Chunk Processor
		// Tasklet <- ChunkProvider <- ItemReader, RepeatTemplate
		// Tasklet <- ChunkProcessor <- ItemProcessor, ItemWriter
		ChunkOrientedTasklet<Product> tasklet = new ChunkOrientedTasklet<Product>(
				new SimpleChunkProvider<Product>(productReader(),
						repeatTemplate),
				new SimpleChunkProcessor<Product, Product>(
						new PassThroughItemProcessor<Product>(),
						new ProductItemWriter()));

		// TODO: Why are the associations required at the end?
		// Job <- Steps <- TaskletStep <- Tasklet
		step.setTasklet(tasklet);
		// Job <- Steps <- TaskletStep
		steps.add(step);
		// Job <- Steps
		bean.setSteps(steps);
        bean.afterPropertiesSet();
        return bean;
	}

	public void setJobRepository(JobRepository jobRepository) {
		this.jobRepository = jobRepository;
	}
}
