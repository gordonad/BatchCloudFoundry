package com.gordondickens.bcf.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.transaction.PlatformTransactionManager;

import javax.inject.Inject;
import javax.sql.DataSource;

@Configuration
public class BatchInfrastructureConfig {
    private static Logger logger = LoggerFactory.getLogger(BatchInfrastructureConfig.class);

    @Inject
    DataSource dataSource;

    @Inject
    PlatformTransactionManager transactionManager;

    @Bean
    public JobExplorer jobExplorer() throws Exception {
        JobExplorerFactoryBean bean = new JobExplorerFactoryBean();
        bean.setDataSource(dataSource);
        bean.afterPropertiesSet();
        return (JobExplorer) bean.getObject();
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean bean = new JobRepositoryFactoryBean();
        bean.setDataSource(dataSource);
        bean.setTransactionManager(transactionManager);
        bean.afterPropertiesSet();
        return bean.getJobRepository();
/*
        <bean id="jobRepository" class="JobRepositoryFactoryBean" factory-method="getJobRepository">
                <property name="dataSource" ...
        </bean>
 */
    }

    @Bean
    public SimpleJobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher bean = new SimpleJobLauncher();
        bean.setJobRepository(jobRepository());
        bean.setTaskExecutor(new SimpleAsyncTaskExecutor());
        bean.afterPropertiesSet();
        return bean;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {

        JobRegistryBeanPostProcessor bean = new JobRegistryBeanPostProcessor();
        bean.setJobRegistry(jobRegistry());
        try {
            bean.afterPropertiesSet();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
        return bean;
    }

    @Bean
    public SimpleJobOperator jobOperator() throws Exception {
        SimpleJobOperator bean = new SimpleJobOperator();
        bean.setJobExplorer(jobExplorer());
        bean.setJobRepository(jobRepository());
        bean.setJobLauncher(jobLauncher());
        bean.setJobRegistry(jobRegistry());
        bean.afterPropertiesSet();
        return bean;
    }

//	@Bean
//	public MBeanExporter mBeanExporter() throws Exception {
//		MBeanExporter bean = new MBeanExporter();
//		Map<String, Object> beans = new HashMap<String, Object>();
//		beans.put("com.gordondickens.bcf:name=jobOperator", jobOperator());
//		bean.setBeans(beans);
//		return bean;
//	}

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
