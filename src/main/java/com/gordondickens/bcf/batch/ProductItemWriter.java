package com.gordondickens.bcf.batch;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.repository.ProductRepository;

public class ProductItemWriter implements ItemWriter<Product> {
	private static final Logger logger = LoggerFactory
			.getLogger(ProductItemWriter.class);

	@Inject
	ProductRepository repository;

	@Override
	public void write(List<? extends Product> items) throws Exception {
		for (Product product : items) {
			logger.debug("***** About to Persist {}", product);
			repository.save(product);
		}
	}
}
