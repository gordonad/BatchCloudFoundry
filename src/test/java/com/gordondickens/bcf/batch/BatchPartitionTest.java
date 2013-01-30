package com.gordondickens.bcf.batch;

import com.gordondickens.bcf.entity.Product;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.PartitionStep;
import org.springframework.batch.core.partition.support.SimplePartitioner;
import org.springframework.batch.core.partition.support.SimpleStepExecutionSplitter;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.dao.MapExecutionContextDao;
import org.springframework.batch.core.repository.dao.MapJobExecutionDao;
import org.springframework.batch.core.repository.dao.MapJobInstanceDao;
import org.springframework.batch.core.repository.dao.MapStepExecutionDao;
import org.springframework.batch.core.repository.support.SimpleJobRepository;
import org.springframework.batch.core.step.AbstractStep;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.integration.partition.BeanFactoryStepLocator;
import org.springframework.batch.integration.partition.MessageChannelPartitionHandler;
import org.springframework.batch.integration.partition.StepExecutionRequest;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.separator.DefaultRecordSeparatorPolicy;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.PassThroughItemProcessor;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.batch.repeat.support.TaskExecutorRepeatTemplate;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.interceptor.WireTap;
import org.springframework.integration.core.AsyncMessagingTemplate;
import org.springframework.integration.core.PollableChannel;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

// This is a Unit Test, do not include Spring Context Config
// Test Execution of PartitionHandler -> Rabbit Gateway -> requestChannel
// Test Receipt of message by Rabbit
// Test Execution of Response from Rabbit
// Test invocation of StepExecutionRequestHandler
public class BatchPartitionTest {
    //TODO - Change from Polling Process
    private static final String STEP_PARTITION = "partitionStep";
    private static final String REMOTE_ENDPOINT = "remoteEndpoint";
    private static final String FILE_TEST_PRODUCTS = "testfiles/products1.txt";
    private static final String AMQP_REQUEST_QUEUE = "request.queue";
    private static final String AMQP_ROUTING_KEY = "batch.job.partition";
    private static final String EXECUTION_CONTEXT = "executionContext";
    private static final String STEP_PROCESS_PRODUCTS = "processProducts";
    private static final String IMPORT_PRODUCTS_JOB = "importProducts";
    private static final String REQUEST_CHANNEL = "requestChannel";
    private static final String RESPONSE_CHANNEL = "responseChannel";

    private static final Logger logger = LoggerFactory
            .getLogger(BatchPartitionTest.class);

    private JobLauncherTestUtils jobLauncherTestUtils;
    private DefaultListableBeanFactory bf;

    @Before
    public void beforeEachTest() throws Exception {
        logger.debug("Before Test");
        bf = new DefaultListableBeanFactory();
        bf.registerSingleton(EXECUTION_CONTEXT, new ExecutionContext());
    }

    @Test
    public void testPropertyFileLoaderJob() {
        try {
            logger.debug("***** Begin Testing File Loader *****");
            configDataSourceAndTrxMgr();
            jobRepositoryExplorerAndDaos();
            stepLocator();
            // setupRabbitMQ();
            jobLauncher();
            logBeanFactory();

            logger.debug("*** Launching Step '{}'", STEP_PARTITION);
            JobExecution exec = jobLauncherTestUtils.launchStep(STEP_PARTITION, bf.getBean(ExecutionContext.class));
            assertNotNull("*** ExecutionContext MUST exist", bf.getBean(EXECUTION_CONTEXT));
            logger.debug("*** Current Execution Context '{}'", bf.getBean(EXECUTION_CONTEXT));

            logger.debug("*** Job Execution Status {}", exec.getExitStatus());
            logJobExecutionFromRepository(exec);
            Assert.assertEquals(BatchStatus.COMPLETED, exec.getStatus());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Assert.fail(e.getMessage());
        }
    }

    private void stepLocator() {
        BeanFactoryStepLocator stepLocator = new BeanFactoryStepLocator();
        stepLocator.setBeanFactory(bf);
        bf.registerSingleton("stepLocator", stepLocator);
    }

    private void jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setTaskExecutor(new SyncTaskExecutor());
        jobLauncher.setJobRepository(bf.getBean(JobRepository.class));
        jobLauncher.afterPropertiesSet();
        jobLauncherTestUtils.setJobLauncher(jobLauncher);
        jobLauncherTestUtils.setJob(createProductsJob());
    }

    private void serviceActivatorEndpoint() {
        ServiceActivatingHandler serviceActivator = new ServiceActivatingHandler(
                createStepExecutionRequestHandler(), "handle");
        serviceActivator.setComponentName(REMOTE_ENDPOINT);
        serviceActivator.setBeanFactory(bf);
        serviceActivator.setShouldTrack(true);


        bf.registerSingleton(REMOTE_ENDPOINT, serviceActivator);
        requestChannel().subscribe(serviceActivator);

        serviceActivator.setOutputChannel(responseChannel());
        serviceActivator.afterPropertiesSet();
    }

    private QueueChannel responseChannel() {
        QueueChannel responseChannel = new QueueChannel();
        responseChannel.setBeanName(RESPONSE_CHANNEL);
        responseChannel.setDatatypes(StepExecution.class);
        responseChannel.addInterceptor(new WireTap(loggingChannel()));
        responseChannel.afterPropertiesSet();
        bf.registerSingleton(RESPONSE_CHANNEL, responseChannel);
        return responseChannel;
    }

    private DirectChannel requestChannel() {
        DirectChannel requestChannel = new DirectChannel();
        requestChannel.setBeanName(REQUEST_CHANNEL);
        requestChannel.setDatatypes(StepExecutionRequest.class);
        requestChannel.addInterceptor(new WireTap(loggingChannel()));
        requestChannel.setShouldTrack(true);
        requestChannel.afterPropertiesSet();
        bf.registerSingleton(REQUEST_CHANNEL, requestChannel);
        return requestChannel;
    }

    private DirectChannel loggingChannel() {
        DirectChannel loggingChannel = new DirectChannel();
        loggingChannel.setBeanName("loggingChannel");
        loggingChannel.setShouldTrack(false);
        loggingChannel.subscribe(messageHistory());
        return loggingChannel;
    }

    private LoggingHandler messageHistory() {
        LoggingHandler loggingHandler = new LoggingHandler("TRACE");
        loggingHandler.setBeanName("loggingHandler");
        loggingHandler.setBeanFactory(bf);
        loggingHandler.setShouldLogFullMessage(true);
        loggingHandler.setShouldTrack(true);
        loggingHandler.afterPropertiesSet();
        return loggingHandler;
    }

    private void jobRepositoryExplorerAndDaos() {
        MapJobInstanceDao jobInstanceDao = new MapJobInstanceDao();
        MapJobExecutionDao jobExecutionDao = new MapJobExecutionDao();
        MapStepExecutionDao stepExecutionDao = new MapStepExecutionDao();
        MapExecutionContextDao executionContextDao = new MapExecutionContextDao();
        bf.registerSingleton("jobInstanceDao", jobInstanceDao);
        bf.registerSingleton("jobExecutionDao", jobExecutionDao);
        bf.registerSingleton("stepExecutionDao", stepExecutionDao);
        bf.registerSingleton("executionContextDao", executionContextDao);

        jobLauncherTestUtils = new JobLauncherTestUtils();
        SimpleJobRepository jobRepository = new SimpleJobRepository(
                jobInstanceDao, jobExecutionDao, stepExecutionDao,
                executionContextDao);
        jobLauncherTestUtils.setJobRepository(jobRepository);
        bf.registerSingleton("jobRepository", jobRepository);

        SimpleJobExplorer jobExplorer = new SimpleJobExplorer(jobInstanceDao,
                jobExecutionDao, stepExecutionDao, executionContextDao);
        bf.registerSingleton("jobExplorer", jobExplorer);
    }

    private void configDataSourceAndTrxMgr() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            stub(dataSource.getConnection()).toReturn(connection);
            stub(connection.getMetaData()).toReturn(databaseMetaData);
        } catch (Exception e) {
            logger.error("Error Occurred Stubbing Datasource or connection", e);
        }

        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager();
        transactionManager
                .setTransactionSynchronization(DataSourceTransactionManager.SYNCHRONIZATION_ON_ACTUAL_TRANSACTION);
        transactionManager = spy(transactionManager);
        transactionManager.setDataSource(dataSource);

        bf.registerSingleton("dataSource", dataSource);
        bf.registerSingleton("transactionManager", transactionManager);
    }

    private void setupRabbitMQ() {
        // Setup Rabbit & AMQP
        ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
        org.springframework.amqp.rabbit.connection.Connection mockConnection = mock(org.springframework.amqp.rabbit.connection.Connection.class);
        when(connectionFactory.createConnection()).thenReturn(mockConnection);

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setQueue(AMQP_REQUEST_QUEUE);
        rabbitTemplate.setRoutingKey(AMQP_ROUTING_KEY);
        rabbitTemplate.setReplyTimeout(1000);
        rabbitTemplate.afterPropertiesSet();
        bf.registerSingleton("rabbitTemplate", rabbitTemplate);
    }

    /**
     * Create the main import Products Job
     *
     * @return
     * @throws Exception
     */
    private SimpleJob createProductsJob() throws Exception {
        // Create Job
        SimpleJob simpleJob = new SimpleJob();
        simpleJob.setName(IMPORT_PRODUCTS_JOB);
        simpleJob.setJobRepository(bf.getBean(JobRepository.class));
        simpleJob.setRestartable(true);

        // Create Steps
        List<Step> steps = new ArrayList<Step>();

        // Create Tasklet Step
        processProductsStep();
        steps.add(bf.getBean(PartitionStep.class));

        // Job <- Steps
        simpleJob.setSteps(steps);
        simpleJob.afterPropertiesSet();
        bf.registerSingleton(IMPORT_PRODUCTS_JOB, simpleJob);
        Collection<String> jobSteps = simpleJob.getStepNames();
        logger.debug("Job '{}' contains {} steps", simpleJob, steps.size());
        for (String stepName : jobSteps) {
            AbstractStep step = (AbstractStep) simpleJob.getStep(stepName);
            logger.debug("\t --> Step Details {}", step);
        }
        return simpleJob;
    }


    /**
     * Partition Step Creator
     *
     * @throws Exception
     */
    // PartitionStep <- (PartitionHandler <- StepExecSplitter) + Partitioner
    private void createPartitionStep() throws Exception {
        serviceActivatorEndpoint();
        assertNotNull("Request Channel MUST be valid",
                bf.getBean(REQUEST_CHANNEL, DirectChannel.class));
        assertNotNull("Response Channel MUST be valid",
                bf.getBean(RESPONSE_CHANNEL, QueueChannel.class));

        PartitionStep partitionStep = new PartitionStep();
        partitionStep.setName(STEP_PARTITION);
        partitionStep.setAllowStartIfComplete(true);
        partitionStep.setPartitionHandler(partitionHandler());
        partitionStep.setStepExecutionSplitter(stepExecutionSplitter());
        partitionStep.setJobRepository(bf.getBean(JobRepository.class));
        partitionStep.setStartLimit(100);
        partitionStep.afterPropertiesSet();
        bf.registerSingleton(STEP_PARTITION, partitionStep);
    }

    /*
    Delegates to SimplePartitioner to generate ExecutionContext instances
     */
    private SimpleStepExecutionSplitter stepExecutionSplitter() throws Exception {
        SimpleStepExecutionSplitter stepExecutionSplitter = new SimpleStepExecutionSplitter(
                bf.getBean(JobRepository.class), true, bf.getBean(
                STEP_PROCESS_PRODUCTS, TaskletStep.class).getName(),
                new SimplePartitioner());
        stepExecutionSplitter.afterPropertiesSet();
        return stepExecutionSplitter;
    }

    private AsyncMessagingTemplate messagingOperations() {
        AsyncMessagingTemplate messagingGateway = new AsyncMessagingTemplate() {
            @Override
            public <P> Message<P> receive() {
                ArrayList<StepExecution> list = new ArrayList<StepExecution>();
                MessageChannel channel = bf.getBean(RESPONSE_CHANNEL, QueueChannel.class);
                return this.receive((PollableChannel) channel);
            }
        };
        messagingGateway.setDefaultChannel(bf.getBean(REQUEST_CHANNEL,
                DirectChannel.class));
        messagingGateway.afterPropertiesSet();
        return messagingGateway;
    }

    private MessageChannelPartitionHandler partitionHandler() throws Exception {
        MessageChannelPartitionHandler partitionHandler = new MessageChannelPartitionHandler();
        partitionHandler.setMessagingOperations(messagingOperations()); // MessageOperations
        partitionHandler.setGridSize(1);
        partitionHandler.setStepName(STEP_PROCESS_PRODUCTS);
        partitionHandler.afterPropertiesSet();
        bf.registerSingleton("partitionHandler", partitionHandler);
        return partitionHandler;
    }


    /*
        This is the inner step, that can be executed on the remote worker
    */
    private void processProductsStep() throws Exception {
        // Create repeat template for Chunk Size
        RepeatTemplate repeatTemplate = new TaskExecutorRepeatTemplate();
        repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(1));

        TaskletStep taskletStep = new TaskletStep();
        taskletStep.setJobRepository(bf.getBean(JobRepository.class));
        taskletStep.setAllowStartIfComplete(true);
        taskletStep.setName(STEP_PROCESS_PRODUCTS);
        taskletStep.setTransactionManager(bf
                .getBean(DataSourceTransactionManager.class));
        taskletStep.setStepOperations(repeatTemplate);

        // Create Chunk Tasklet with Provider (reader) and Chunk Processor
        ChunkOrientedTasklet<Product> RPWtasklet = new ChunkOrientedTasklet<Product>(
                new SimpleChunkProvider<Product>(productReader(),
                        repeatTemplate),
                new SimpleChunkProcessor<Product, Product>(
                        new PassThroughItemProcessor<Product>(),
                        new ProductItemLoggerWriter()));

        taskletStep.setTasklet(RPWtasklet);
        taskletStep.setAllowStartIfComplete(true);

        bf.registerSingleton("chunkTasklet", RPWtasklet);
        bf.registerSingleton(STEP_PROCESS_PRODUCTS, taskletStep);
        // Create Partition Step Handler
        createPartitionStep();
        // Job <- Steps <- TaskletStep <- Tasklet
        taskletStep.setTasklet(bf.getBean(ChunkOrientedTasklet.class));
        taskletStep.afterPropertiesSet();
        // Job <- Steps <- TaskletStep
        // steps.add(step);
        // Job <- Steps <- PartitionStep

    }

    private LineMapper<Product> productLineMapper() {
        DefaultLineMapper<Product> bean = new DefaultLineMapper<Product>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter(DelimitedLineTokenizer.DELIMITER_COMMA);
        tokenizer.setNames(new String[]{"productId", "store", "quantity",
                "description"});
        bean.setLineTokenizer(tokenizer);
        bean.setFieldSetMapper(new ProductFieldSetMapper());
        bean.afterPropertiesSet();
        return bean;
    }

    private FlatFileItemReader<Product> productReader() throws Exception {
        FlatFileItemReader<Product> bean = new FlatFileItemReader<Product>();
        DefaultRecordSeparatorPolicy defaultRecordSeparatorPolicy = new DefaultRecordSeparatorPolicy(
                "'", "\n");
        bean.setRecordSeparatorPolicy(defaultRecordSeparatorPolicy);
        Resource resource = new ClassPathResource(FILE_TEST_PRODUCTS);
        bean.setResource(resource);
        bean.setStrict(true);
        bean.setLinesToSkip(1);
        bean.setLineMapper(productLineMapper());

        bean.afterPropertiesSet();
        bean.open(bf.getBean(EXECUTION_CONTEXT, ExecutionContext.class));
        return bean;
    }

    private StepExecutionRequestHandler createStepExecutionRequestHandler() {
        assertNotNull("JobExplorer MUST exist", bf.getBean(JobExplorer.class));
        assertNotNull("stepLocator MUST exist",
                bf.getBean(BeanFactoryStepLocator.class));
        StepExecutionRequestHandler stepExecutionRequestHandler = new StepExecutionRequestHandler();
        stepExecutionRequestHandler.setJobExplorer(bf
                .getBean(JobExplorer.class));
        stepExecutionRequestHandler.setStepLocator(bf
                .getBean(BeanFactoryStepLocator.class));
        return stepExecutionRequestHandler;
    }

    /**
     * Logger for Job Execution
     *
     * @param exec
     */
    private void logJobExecutionFromRepository(JobExecution exec) {
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

    private void logBeanFactory() {

        logger.debug("*** Number of Beans in context is {}", bf.getBeanDefinitionCount());

        logger.debug("*****BEANS in BF {}", bf.toString());

        String[] beans = bf.getBeanDefinitionNames();
        for (String o : beans) {
            logger.debug("________________________");
            logger.debug("BEAN = " + o);
            logger.debug("\tType = " + bf.getType(o));
            String[] aliases = bf.getAliases(o);
            if (aliases != null && aliases.length > 0) {
                for (String a : aliases) {
                    logger.debug("\tAliased as: " + a);
                }
            }
        }
    }


}
