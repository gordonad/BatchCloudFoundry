package com.gordondickens.bcf.config;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchInfrastructureConfig {

	@Inject
	DataSource dataSource;

	@Inject
	PlatformTransactionManager transactionManager;

	@Bean
	public SimpleAsyncTaskExecutor taskExecutor() {
		return new SimpleAsyncTaskExecutor();
	}

	@Bean
	public JobExplorer jobExplorer() throws Exception {
		JobExplorerFactoryBean bean = new JobExplorerFactoryBean();
		bean.setDataSource(dataSource);
		return (JobExplorer) bean.getObject();
	}

	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean bean = new JobRepositoryFactoryBean();
		bean.setDataSource(dataSource);
		bean.setTransactionManager(transactionManager);
		return bean.getJobRepository();
	}

	@Bean
	public SimpleJobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher bean = new SimpleJobLauncher();
		bean.setJobRepository(jobRepository());
		bean.setTaskExecutor(taskExecutor());
		return bean;
	}

	@Bean
	public JobRegistry jobRegistry() {
		JobRegistry bean = new MapJobRegistry();
		return bean;
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
		JobRegistryBeanPostProcessor bean = new JobRegistryBeanPostProcessor();
		bean.setJobRegistry(jobRegistry());
		return bean;
	}

	@Bean
	public SimpleJobOperator jobOperator() throws Exception {
		SimpleJobOperator bean = new SimpleJobOperator();
		bean.setJobExplorer(jobExplorer());
		bean.setJobRepository(jobRepository());
		bean.setJobLauncher(jobLauncher());
		bean.setJobRegistry(jobRegistry());
		return bean;
	}

	@Bean
	public MBeanExporter mBeanExporter() throws Exception {
		MBeanExporter bean = new MBeanExporter();
		Map<String, Object> beans = new HashMap<String, Object>();
		beans.put("com.gordondickens.bcf:name=jobOperator", jobOperator());
		bean.setBeans(beans);
		return bean;
	}

	// For Partitioned Jobs, do not need FaultTolerant steps, FactoryBean
	// unnecessary.
	// @Bean
	// public TaskletStep simpleStep() {
	// SimpleStepFactoryBean factory = new SimpleStepFactoryBean<T, S>();
	// factory.setTransactionManager(transactionManager);
	// factory.setJobRepository(jobRepository());
	// factory.setStartLimit(100);
	// factory.setCommitInterval(1);
	// TaskletStep bean = (TaskletStep) factory.getObject();
	// return bean;
	// }

}
