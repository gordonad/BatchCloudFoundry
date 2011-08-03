package com.gordondickens.bcf.batch;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.repository.ProductRepository;

public class ProductItemWriter implements ItemWriter<Product> {
	private static final Logger logger = LoggerFactory
			.getLogger(ProductItemWriter.class);

	@Autowired
	ProductRepository repository;

	@Override
	public void write(List<? extends Product> items) throws Exception {
		Product product = null;

		for (Product p : items) {
			product = p;
			logger.debug("***** About to Persist {}", product);
			repository.save(product);
		}
		// if (product != null) {
		// try {
		// repository.flush();
		// } finally {
		// repository.clear();
		// }
		// }
	}
}
