package com.gordondickens.bcf.batch;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.repository.ProductRepository;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class BatchProductTest {
	private static final Logger logger = LoggerFactory
			.getLogger(BatchProductTest.class);

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	ProductRepository repository;

	@Before
	public void beforeEachTest() {
		logger.debug("Before Test");
	}

	@Test
	public void testPropertyFileLoaderJob() {
		try {
			logger.debug("Begin Testing File Loader");
			JobExecution exec = jobLauncherTestUtils.launchJob();
			Assert.assertTrue("Product Repository MUST have records",
					repository.count() > 0);
			List<Product> products = repository.findAllProducts();
			Assert.assertNotNull("List of Products MUST exist", products);
			for (Product p : products) {
				logger.debug("Retrieved Product {}", p);
			}
			Assert.assertEquals(BatchStatus.COMPLETED, exec.getStatus());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Assert.fail(e.getMessage());
		}
		// Resource ouput= new
		// FileSystemResource("./target/outputs/delimited-beanwrapperextractor.txt");
	}
}
