package com.gordondickens.bcf.batch;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.dao.MapExecutionContextDao;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.RecordSeparatorPolicy;
import org.springframework.batch.item.file.separator.SimpleRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.repeat.support.TaskExecutorRepeatTemplate;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.gordondickens.bcf.config.ProductJobConfig;
import com.gordondickens.bcf.entity.Product;

// This is a Unit Test, do not include Spring Context Config
public class BatchProductTest {
	private static final Logger logger = LoggerFactory
			.getLogger(BatchProductTest.class);

	private JobLauncherTestUtils jobLauncherTestUtils;
	private SimpleJobRepository jobRepository;
	private ProductJobConfig jobConfig;
	private DataSource dataSource;
	private DataSourceTransactionManager transactionManager;
	private ExecutionContext executionContext;

	@Before
	public void beforeEachTest() {
		logger.debug("Before Test");

		dataSource = (DataSource) Mockito.mock(DataSource.class);
		Connection connection = (Connection) Mockito.mock(Connection.class);
		DatabaseMetaData databaseMetaData = (DatabaseMetaData) Mockito
				.mock(DatabaseMetaData.class);
		try {
			Mockito.stub(dataSource.getConnection()).toReturn(connection);
			Mockito.stub(connection.getMetaData()).toReturn(databaseMetaData);
		} catch (Exception e) {
			logger.error("Error Occurred Stubbing Datasource or connection", e);
		}
		transactionManager = new DataSourceTransactionManager();
		transactionManager
				.setTransactionSynchronization(DataSourceTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
		transactionManager = Mockito.spy(transactionManager);
		transactionManager.setDataSource(dataSource);

		MapJobInstanceDao jobInstanceDao = new MapJobInstanceDao();
		MapJobExecutionDao jobExecutionDao = new MapJobExecutionDao();
		MapStepExecutionDao stepExecutionDao = new MapStepExecutionDao();
		MapExecutionContextDao ecDao = new MapExecutionContextDao();

		jobLauncherTestUtils = new JobLauncherTestUtils();
		jobRepository = new SimpleJobRepository(jobInstanceDao,
				jobExecutionDao, stepExecutionDao, ecDao);

		jobLauncherTestUtils.setJobRepository(jobRepository);
		jobConfig = new ProductJobConfig();
		jobConfig.setJobRepository(jobRepository);
		executionContext = new ExecutionContext();
	}

	@Test
	public void testPropertyFileLoaderJob() {
		try {
			logger.debug("Begin Testing File Loader");
			SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
			jobLauncher.setTaskExecutor(new SyncTaskExecutor());

			jobLauncher.setJobRepository(jobRepository);
			jobLauncherTestUtils.setJobLauncher(jobLauncher);

			SimpleJob job = importProductsJob();
			logJobDetails(job);
			jobLauncherTestUtils.setJob(job);
			JobExecution exec = jobLauncherTestUtils
					.launchJob(new JobParameters());

			Collection<String> stepNames = job.getStepNames();
			for (String stepName : stepNames) {
				TaskletStep step = (TaskletStep) job.getStep(stepName);
				step.setTransactionManager(transactionManager);
			}
			assertTrue("Steps MUST exist", stepNames != null);
			assertTrue("At least ONE step MUST exist", stepNames.size() > 0);

			Assert.assertTrue("Product Repository MUST have records",
					jobRepository.isJobInstanceExists(job.getName(),
							new JobParameters()));

			logger.debug("Job Execution Status {}", exec.getExitStatus());
			logJobRepository(exec);
			Assert.assertEquals(BatchStatus.COMPLETED, exec.getStatus());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
	}

	private void logJobRepository(JobExecution exec) {
		logger.debug("*** Logging Job Repository Results ***");
		List<Throwable> failures = exec.getAllFailureExceptions();
		if (failures != null && !failures.isEmpty()) {
			for (Throwable t : failures) {
				logger.error("Throwable Message:", t);
			}
		}

		Date createDate = exec.getCreateTime();
		Date endDate = exec.getEndTime();
		logger.debug("Create Date '{}' - End Date '{}'", createDate, endDate);
		Long execId = exec.getId();
		Long jobId = exec.getJobId();
		logger.debug("Exec Id '{}' - Job Id '{}'", execId, jobId);
		BatchStatus status = exec.getStatus();
	}

	private void logJobDetails(SimpleJob job) {
		logger.debug("*** Logging Job Details for {} ****", job);
		Collection<String> steps = job.getStepNames();
		logger.debug("Job {} contains {} steps", job.getName(), steps.size());
		for (String stepName : steps) {
			logger.debug("\t --> Step Name '{}'", job.getName(), stepName);
			TaskletStep step = (TaskletStep) job.getStep(stepName);
			logger.debug("\t --> Step Details {}", step);

		}
	}

	private SimpleJob importProductsJob() throws Exception {
		// Create Job
		SimpleJob bean = new SimpleJob();
		bean.setName("importProductsJob");
		bean.setJobRepository(jobRepository);

		// Create Steps
		List<Step> steps = new ArrayList<Step>();

		// Create Tasklet Step
		TaskletStep step = new TaskletStep();
		step.setName("importProducts");
		step.setTransactionManager(transactionManager);
		step.setJobRepository(jobRepository);
		step.setStartLimit(100);

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
						new ProductItemLoggerWriter()));

		// Job <- Steps <- TaskletStep <- Tasklet
		step.setTasklet(tasklet);
		// Job <- Steps <- TaskletStep
		steps.add(step);
		// Job <- Steps
		bean.setSteps(steps);
		return bean;
	}

	private LineMapper<Product> productLineMapper() {
		DefaultLineMapper<Product> bean = new DefaultLineMapper<Product>();
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
		tokenizer.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);
		tokenizer.setNames(new String[] { "productId", "store", "quantity",
				"description" });
		bean.setLineTokenizer(tokenizer);
		bean.setFieldSetMapper(new ProductFieldSetMapper());
		return bean;

	}

	private FlatFileItemReader<Product> productReader() throws Exception {
		FlatFileItemReader<Product> bean = new FlatFileItemReader<Product>();
		DefaultRecordSeparatorPolicy defaultRecordSeparatorPolicy = new DefaultRecordSeparatorPolicy(
				"'", "\n");
		bean.setRecordSeparatorPolicy(defaultRecordSeparatorPolicy);
		Resource resource = new ClassPathResource("testfiles/products1.txt");
		bean.setResource(resource);
		bean.setStrict(true);
		bean.setLinesToSkip(1);
		bean.setLineMapper(productLineMapper());

		bean.afterPropertiesSet();
		bean.open(executionContext);
		return bean;
	}
}
